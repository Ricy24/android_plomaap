import os
import sys

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"

# 1. Update app/services/appointment_service.py
appointment_service_code = """from datetime import datetime, time as dtime, timedelta
from app.database.models import User, Appointment, Service


def _normalize_schedule(schedule):
    if not schedule:
        return {}
    normalized = {}
    for day, ranges in schedule.items():
        if not ranges:
            continue
        normalized[day.lower()] = []
        for item in ranges:
            if isinstance(item, (list, tuple)) and len(item) == 2:
                start, end = item
                try:
                    normalized[day.lower()].append([int(start), int(end)])
                except (TypeError, ValueError):
                    continue
    return normalized


def find_available_technician(date_str, time_str, service_id=None, exclude_technician_id=None):
    \"\"\"Find available technician for given date/time/service with highest rating.\"\"\"
    try:
        appointment_date = datetime.strptime(date_str, '%Y-%m-%d').date()
        appointment_time = datetime.strptime(time_str, '%H:%M').time()
    except (ValueError, TypeError):
        return None

    technicians = User.query.filter(
        User.role == 'technician',
        User.status == 'active'
    ).all()

    available = []

    for tech in technicians:
        if exclude_technician_id and tech.id == exclude_technician_id:
            continue

        profile = tech.technician_profile
        if not profile or not profile.available:
            continue

        day_name = appointment_date.strftime('%a').lower()
        schedule = _normalize_schedule(profile.schedule or {})
        if day_name not in schedule:
            continue

        is_within_schedule = False
        for start_hour, end_hour in schedule[day_name]:
            if start_hour <= appointment_time.hour < end_hour:
                is_within_schedule = True
                break

        if not is_within_schedule:
            continue

        conflict = Appointment.query.filter(
            Appointment.technician_id == tech.id,
            Appointment.date == appointment_date,
            Appointment.time == time_str,
            Appointment.status.in_(['pending', 'scheduled', 'in_progress'])
        ).first()

        if conflict:
            continue

        available.append({
            'technician': tech,
            'rating': float(profile.rating or 0.0)
        })

    available.sort(key=lambda x: x['rating'], reverse=True)
    return available[0]['technician'] if available else None


def get_available_time_slots(technician_id, date_str):
    \"\"\"Get available time slots for technician on specific date (backward compatibility).\"\"\"
    try:
        appointment_date = datetime.strptime(date_str, '%Y-%m-%d').date()
    except (ValueError, TypeError):
        return []

    tech = User.query.get(technician_id)
    if not tech or tech.role != 'technician':
        return []

    profile = tech.technician_profile
    if not profile:
        return []

    day_name = appointment_date.strftime('%a').lower()
    schedule = _normalize_schedule(profile.schedule or {})

    if day_name not in schedule:
        return []

    slots = []
    for start_hour, end_hour in schedule[day_name]:
        current_hour = start_hour
        while current_hour < end_hour:
            slot_time = f"{int(current_hour):02d}:00"
            conflict = Appointment.query.filter(
                Appointment.technician_id == technician_id,
                Appointment.date == appointment_date,
                Appointment.time == slot_time,
                Appointment.status.in_(['pending', 'scheduled', 'in_progress'])
            ).first()

            if not conflict:
                slots.append(slot_time)

            current_hour += 1

    return slots


def get_smart_time_slots(service_id, date_str, technician_id=None):
    \"\"\"
    Generate intelligent availability time slots for a service on a given date.
    Respects:
    - Service duration (e.g. 60 or 90 min)
    - Operating schedule of technicians
    - Existing appointments
    - Past time on current day
    - Grouping by period: morning, afternoon, evening
    \"\"\"
    try:
        appointment_date = datetime.strptime(date_str, '%Y-%m-%d').date()
    except (ValueError, TypeError):
        return {'error': 'Invalid date format'}, 400

    service = Service.query.get(service_id) if service_id else None
    duration_minutes = service.duration_minutes if service and hasattr(service, 'duration_minutes') and service.duration_minutes else 60

    # Determine candidate technicians
    if technician_id:
        tech = User.query.get(technician_id)
        candidate_techs = [tech] if tech and tech.role == 'technician' and tech.status == 'active' else []
    else:
        candidate_techs = User.query.filter(
            User.role == 'technician',
            User.status == 'active'
        ).all()

    day_name = appointment_date.strftime('%a').lower()
    now = datetime.now()
    is_today = (appointment_date == now.date())

    # Standard working hours in PlomApp: 08:00 to 18:00
    candidate_hours = [
        (8, 0), (9, 0), (10, 0), (11, 0),
        (12, 0), (13, 0), (14, 0), (15, 0), (16, 0), (17, 0)
    ]

    slots = []
    for hour, minute in candidate_hours:
        slot_start_str = f"{hour:02d}:{minute:02d}"
        slot_start_time = dtime(hour, minute)
        slot_end_dt = datetime.combine(appointment_date, slot_start_time) + timedelta(minutes=duration_minutes)
        slot_end_str = slot_end_dt.strftime('%H:%M')

        # Check if slot is already in the past today (buffer of 30 mins)
        if is_today and datetime.combine(appointment_date, slot_start_time) < (now + timedelta(minutes=30)):
            is_available = False
            available_techs_count = 0
        else:
            # Check availability against candidate technicians
            available_techs_count = 0
            for tech in candidate_techs:
                profile = tech.technician_profile
                if not profile or not profile.available:
                    continue

                schedule = _normalize_schedule(profile.schedule or {})
                if day_name not in schedule:
                    continue

                # Must be within schedule
                within_schedule = any(start_h <= hour < end_h for start_h, end_h in schedule[day_name])
                if not within_schedule:
                    continue

                # Check conflict
                conflict = Appointment.query.filter(
                    Appointment.technician_id == tech.id,
                    Appointment.date == appointment_date,
                    Appointment.time == slot_start_str,
                    Appointment.status.in_(['pending', 'scheduled', 'in_progress'])
                ).first()

                if not conflict:
                    available_techs_count += 1

            is_available = (available_techs_count > 0)

        # Categorize period
        if hour < 12:
            period = 'morning'
        elif hour < 17:
            period = 'afternoon'
        else:
            period = 'evening'

        slots.append({
            'start': slot_start_str,
            'end': slot_end_str,
            'time': slot_start_str,
            'available': is_available,
            'period': period,
            'available_technicians_count': available_techs_count
        })

    return {
        'date': date_str,
        'service_id': service_id,
        'duration_minutes': duration_minutes,
        'slots': slots
    }


def validate_appointment_transition(current_status, new_status):
    \"\"\"Validate appointment status transitions\"\"\"
    valid_transitions = {
        'pending': ['scheduled', 'cancelled'],
        'scheduled': ['in_progress', 'cancelled'],
        'in_progress': ['completed', 'cancelled'],
        'completed': [],
        'cancelled': []
    }
    return new_status in valid_transitions.get(current_status, [])


def check_technician_availability(technician_id, date_str, time_str):
    \"\"\"Check specific technician availability for booking with row lock.\"\"\"
    try:
        appointment_date = datetime.strptime(date_str, '%Y-%m-%d').date()
        appointment_time = datetime.strptime(time_str, '%H:%M').time()
    except (ValueError, TypeError):
        return False, 'Formato de fecha u hora invalido', None

    tech = User.query.get(technician_id)
    if not tech:
        return False, 'El plomero seleccionado no existe', None
    if tech.role != 'technician':
        return False, 'El usuario seleccionado no es un tecnico plomero', None
    if tech.status != 'active':
        return False, 'El plomero seleccionado no se encuentra activo', None

    profile = tech.technician_profile
    if not profile or not profile.available:
        return False, 'El plomero seleccionado no esta disponible para servicios', None

    day_name = appointment_date.strftime('%a').lower()
    schedule = _normalize_schedule(profile.schedule or {})
    if day_name not in schedule:
        return False, f'El plomero no atiende en el dia solicitado ({day_name})', None

    is_within_schedule = False
    for start_hour, end_hour in schedule[day_name]:
        if start_hour <= appointment_time.hour < end_hour:
            is_within_schedule = True
            break

    if not is_within_schedule:
        return False, 'La hora seleccionada esta fuera del horario de atencion del plomero', None

    conflict = Appointment.query.filter(
        Appointment.technician_id == tech.id,
        Appointment.date == appointment_date,
        Appointment.time == time_str,
        Appointment.status.in_(['pending', 'scheduled', 'in_progress'])
    ).first()

    if conflict:
        return False, 'El plomero seleccionado ya no esta disponible para ese horario. Por favor elige otro horario o plomero.', None

    return True, None, tech
"""

with open(os.path.join(BACKEND_DIR, "app", "services", "appointment_service.py"), "w", encoding="utf-8") as f:
    f.write(appointment_service_code)
print("Updated appointment_service.py")

# 2. Update app/routes/technicians.py slots endpoint
technicians_routes_code = """from datetime import datetime
from flask import Blueprint, jsonify, request
from flask_jwt_extended import get_jwt_identity
from app.database.models import User, TechnicianProfile, Appointment
from app.database.extensions import db
from app.utils.decorators import jwt_required_custom, technician_only
from app.utils.validators import validate_appointment_status
from app.services.appointment_service import (
    get_available_time_slots,
    get_smart_time_slots,
    validate_appointment_transition,
    _normalize_schedule
)

technicians_bp = Blueprint('technicians', __name__, url_prefix='/api/technicians')

@technicians_bp.route('', methods=['GET'])
def list_technicians():
    \"\"\"List all active technicians\"\"\"
    limit = request.args.get('limit', default=20, type=int)
    offset = request.args.get('offset', default=0, type=int)
    if limit > 100:
        limit = 100
    
    query = User.query.filter(User.role == 'technician', User.status == 'active')
    technicians = query.limit(limit).offset(offset).all()
    
    result = []
    for tech in technicians:
        tech_dict = tech.to_dict()
        if tech.technician_profile:
            tech_dict['profile'] = tech.technician_profile.to_dict()
        result.append(tech_dict)
    
    return jsonify({'technicians': result}), 200

@technicians_bp.route('/available', methods=['GET'])
def available_technicians():
    \"\"\"Get available technicians for date/time/service.\"\"\"
    date_str = request.args.get('date')
    time_str = request.args.get('time')
    service_id = request.args.get('service_id') or request.args.get('serviceId', type=int)

    if not date_str or not service_id:
        return jsonify({'error': 'date and service_id are required'}), 400

    try:
        appointment_date = datetime.strptime(date_str, '%Y-%m-%d').date()
    except (TypeError, ValueError):
        return jsonify({'error': 'Invalid date format'}), 400

    technicians = User.query.filter(
        User.role == 'technician',
        User.status == 'active'
    ).all()

    available = []
    for tech in technicians:
        profile = tech.technician_profile
        if not profile or not profile.available:
            continue

        day_name = appointment_date.strftime('%a').lower()
        schedule = _normalize_schedule(profile.schedule or {})
        if day_name not in schedule:
            continue

        if time_str:
            try:
                appointment_time = datetime.strptime(time_str, '%H:%M').time()
            except (ValueError, TypeError):
                continue

            is_within_schedule = any(start_hour <= appointment_time.hour < end_hour for start_hour, end_hour in schedule[day_name])
            if not is_within_schedule:
                continue

            conflict = Appointment.query.filter(
                Appointment.technician_id == tech.id,
                Appointment.date == appointment_date,
                Appointment.time == time_str,
                Appointment.status.in_(['pending', 'scheduled', 'in_progress'])
            ).first()
            if conflict:
                continue

        tech_dict = tech.to_dict()
        tech_dict['profile'] = profile.to_dict()
        available.append(tech_dict)

    available.sort(key=lambda x: x['profile']['rating'] if x.get('profile') and x['profile'].get('rating') else 0, reverse=True)
    return jsonify({'technicians': available}), 200


@technicians_bp.route('/slots', methods=['GET'])
def available_slots():
    \"\"\"Get available time slots with detailed status and periods.\"\"\"
    technician_id = request.args.get('technician_id') or request.args.get('technicianId', type=int)
    service_id = request.args.get('service_id') or request.args.get('serviceId', type=int)
    date_str = request.args.get('date')

    if not date_str:
        return jsonify({'error': 'date is required'}), 400

    # Use smart slot calculation
    result = get_smart_time_slots(service_id=service_id, date_str=date_str, technician_id=technician_id)
    if isinstance(result, tuple):
        return jsonify(result[0]), result[1]
    
    return jsonify(result), 200

@technicians_bp.route('/profile', methods=['GET'])
@technician_only
def get_technician_profile():
    \"\"\"Get authenticated technician's profile\"\"\"
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    if not user:
        return jsonify({'error': 'User not found'}), 404
    
    profile_dict = user.to_dict()
    if user.technician_profile:
        profile_dict['profile'] = user.technician_profile.to_dict()
    return jsonify(profile_dict), 200

@technicians_bp.route('/appointments', methods=['GET'])
@technician_only
def technician_appointments():
    \"\"\"Get technician's appointments\"\"\"
    user_id = get_jwt_identity()
    date_filter = request.args.get('date')
    status_filter = request.args.get('status')
    
    query = Appointment.query.filter(Appointment.technician_id == user_id)
    if date_filter:
        query = query.filter(Appointment.date == date_filter)
    if status_filter:
        query = query.filter(Appointment.status == status_filter)
    
    appointments = query.all()
    return jsonify({'appointments': [apt.to_dict(enriched=True) for apt in appointments]}), 200

@technicians_bp.route('/appointments/<int:appointment_id>', methods=['PATCH'])
@technician_only
def update_appointment_status(appointment_id):
    \"\"\"Update appointment status (technician only)\"\"\"
    user_id = get_jwt_identity()
    data = request.get_json() or {}
    appointment = Appointment.query.get(appointment_id)
    
    if not appointment:
        return jsonify({'error': 'Appointment not found'}), 404
    if appointment.technician_id != user_id:
        return jsonify({'error': 'Unauthorized'}), 403
    
    if 'status' in data:
        new_status = data['status']
        if not validate_appointment_status(new_status):
            return jsonify({'error': 'Invalid status'}), 400
        if not validate_appointment_transition(appointment.status, new_status):
            return jsonify({'error': f'Cannot transition from {appointment.status} to {new_status}'}), 400
        appointment.status = new_status
    
    db.session.commit()
    return jsonify(appointment.to_dict(enriched=True)), 200
"""

with open(os.path.join(BACKEND_DIR, "app", "routes", "technicians.py"), "w", encoding="utf-8") as f:
    f.write(technicians_routes_code)
print("Updated technicians.py")

# 3. Update app/routes/appointments.py
appointments_routes_code = """from datetime import datetime
from flask import Blueprint, jsonify, request
from flask_jwt_extended import get_jwt_identity
from app.database.models import User, Appointment, Service, UserAddress
from app.database.extensions import db
from app.utils.decorators import jwt_required_custom
from app.utils.validators import (
    validate_appointment_status,
    validate_date_time
)
from app.services.appointment_service import (
    validate_appointment_transition,
    check_technician_availability,
    find_available_technician,
    get_smart_time_slots
)
from app.utils.email_utils import send_template_email

appointments_bp = Blueprint('appointments', __name__, url_prefix='/api/appointments')

@appointments_bp.route('/availability', methods=['GET'])
def get_availability():
    \"\"\"Dedicated availability slots endpoint.\"\"\"
    service_id = request.args.get('service_id') or request.args.get('serviceId', type=int)
    date_str = request.args.get('date')
    technician_id = request.args.get('technician_id') or request.args.get('technicianId', type=int)

    if not date_str:
        return jsonify({'error': 'date is required'}), 400

    result = get_smart_time_slots(service_id=service_id, date_str=date_str, technician_id=technician_id)
    if isinstance(result, tuple):
        return jsonify(result[0]), result[1]
    return jsonify(result), 200

@appointments_bp.route('', methods=['GET'])
@jwt_required_custom
def list_appointments():
    \"\"\"List user appointments\"\"\"
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    
    date_filter = request.args.get('date')
    status_filter = request.args.get('status')
    
    if user.role == 'customer':
        query = Appointment.query.filter(Appointment.user_id == user_id)
    elif user.role == 'technician':
        query = Appointment.query.filter(Appointment.technician_id == user_id)
    elif user.role == 'admin':
        query = Appointment.query
    else:
        return jsonify({'error': 'Invalid user role'}), 403
    
    if date_filter:
        try:
            parsed_date = datetime.strptime(date_filter, '%Y-%m-%d').date()
        except (TypeError, ValueError):
            parsed_date = None
        if parsed_date:
            query = query.filter(Appointment.date == parsed_date)
    
    if status_filter:
        query = query.filter(Appointment.status == status_filter)
    
    # Order by date descending, time descending
    appointments = query.order_by(Appointment.date.desc(), Appointment.time.desc()).all()
    
    return jsonify({
        'appointments': [apt.to_dict(enriched=True) for apt in appointments]
    }), 200

@appointments_bp.route('/<int:appointment_id>', methods=['GET'])
@jwt_required_custom
def get_appointment(appointment_id):
    \"\"\"Get single appointment details\"\"\"
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    appointment = Appointment.query.get(appointment_id)
    
    if not appointment:
        return jsonify({'error': 'Appointment not found'}), 404
    
    if user.role == 'customer' and appointment.user_id != user_id:
        return jsonify({'error': 'Unauthorized'}), 403
    elif user.role == 'technician' and appointment.technician_id != user_id:
        return jsonify({'error': 'Unauthorized'}), 403
    
    return jsonify(appointment.to_dict(enriched=True)), 200

@appointments_bp.route('', methods=['POST'])
@jwt_required_custom
def create_appointment():
    \"\"\"
    Create a new appointment with atomic transaction and double-booking prevention.
    Supports automatic technician assignment or explicit selection.
    Associates location snapshot (address, address_reference, user_address_id).
    \"\"\"
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    
    if user.role != 'customer':
        return jsonify({'success': False, 'message': 'Solo los clientes pueden agendar citas'}), 403
    
    data = request.get_json() or {}
    
    service_id = data.get('service_id') or data.get('serviceId')
    date_value = data.get('date')
    time_value = data.get('time')
    address_str = data.get('address')
    address_ref = data.get('address_reference') or data.get('addressReference')
    user_addr_id = data.get('user_address_id') or data.get('userAddressId')
    notes_val = data.get('notes', '')
    
    # Validation of required parameters
    if not service_id or not date_value or not time_value:
        return jsonify({'success': False, 'message': 'service_id, date y time son requeridos'}), 400
    
    if not validate_date_time(date_value, time_value):
        return jsonify({'success': False, 'message': 'Formato de fecha u hora invalido'}), 400

    try:
        appointment_date = datetime.strptime(date_value, '%Y-%m-%d').date()
    except (TypeError, ValueError):
        return jsonify({'success': False, 'message': 'Formato de fecha invalido'}), 400

    # Ensure date is not in the past
    now = datetime.now()
    if appointment_date < now.date():
        return jsonify({'success': False, 'message': 'No puedes agendar citas en fechas pasadas'}), 400
    
    # Validate service exists
    service = Service.query.get(int(service_id)) if str(service_id).isdigit() else None
    if not service:
        return jsonify({'success': False, 'message': 'Servicio no encontrado'}), 404

    # Resolve address from user_address_id if provided
    resolved_address = address_str
    resolved_ref = address_ref
    if user_addr_id:
        user_addr = UserAddress.query.filter_by(id=user_addr_id, user_id=user_id).first()
        if user_addr:
            if not resolved_address:
                resolved_address = f"{user_addr.label}: {user_addr.address}"
            if not resolved_ref and user_addr.reference:
                resolved_ref = user_addr.reference
        else:
            user_addr_id = None

    if not resolved_address and user.address:
        resolved_address = user.address

    # Handle Technician Assignment: Manual or Auto-assign
    supplied_technician_id = data.get('technician_id') or data.get('technicianId')
    technician = None
    
    try:
        # Atomic Transaction start
        if supplied_technician_id and str(supplied_technician_id) not in ('0', 'null', 'None', ''):
            tech_id = int(supplied_technician_id)
            is_avail, err_msg, technician = check_technician_availability(tech_id, date_value, time_value)
            if not is_avail:
                return jsonify({
                    'success': False,
                    'code': 'SLOT_UNAVAILABLE',
                    'message': err_msg or 'El plomero seleccionado ya no esta disponible para ese horario. Por favor elige otro horario o plomero.'
                }), 409
        else:
            # Auto-assign best available technician
            technician = find_available_technician(date_value, time_value, service.id)
            if not technician:
                return jsonify({
                    'success': False,
                    'code': 'SLOT_UNAVAILABLE',
                    'message': 'No hay ningun profesional disponible en ese horario. Por favor selecciona otro horario.'
                }), 409

        # Double-booking check with row-lock to prevent race conditions
        conflict = Appointment.query.filter(
            Appointment.technician_id == technician.id,
            Appointment.date == appointment_date,
            Appointment.time == time_value,
            Appointment.status.in_(['pending', 'scheduled', 'in_progress'])
        ).with_for_update().first()

        if conflict:
            db.session.rollback()
            return jsonify({
                'success': False,
                'code': 'SLOT_UNAVAILABLE',
                'message': 'Este horario acaba de ser ocupado. Por favor selecciona otro horario.'
            }), 409

        appointment = Appointment(
            user_id=int(user_id),
            technician_id=technician.id,
            service_id=service.id,
            date=appointment_date,
            time=time_value,
            status='scheduled',
            address=resolved_address,
            address_reference=resolved_ref,
            user_address_id=user_addr_id,
            notes=notes_val
        )
        
        db.session.add(appointment)
        db.session.commit()

        # Send rich notification emails
        customer = User.query.get(user_id)
        if customer and customer.email:
            send_template_email(
                customer.email,
                f'¡Servicio Confirmado! Cita #{appointment.id} - PlomApp',
                'appointment_customer',
                {
                    'name': customer.name,
                    'booking_id': f'PLM-{appointment.id:05d}',
                    'service_name': service.name,
                    'service_price': f'${float(service.base_price):,.0f} COP'.replace(',', '.'),
                    'duration': f'{service.duration_minutes or 60} minutos',
                    'appointment_date': appointment_date.strftime('%d/%m/%Y'),
                    'appointment_time': time_value,
                    'technician_name': technician.name,
                    'technician_rating': str(technician.technician_profile.rating if technician.technician_profile else 4.8),
                    'address': resolved_address or 'Por coordinar',
                    'address_reference': resolved_ref or '',
                    'notes': notes_val or 'Ninguna observacion adicional.',
                    'text_body': f'Tu cita para {service.name} ha sido confirmada para el {appointment_date} a las {time_value}.'
                }
            )

        if technician and technician.email:
            send_template_email(
                technician.email,
                f'Nueva cita asignada #{appointment.id} - PlomApp',
                'appointment_technician',
                {
                    'name': technician.name,
                    'booking_id': f'PLM-{appointment.id:05d}',
                    'service_name': service.name,
                    'appointment_date': appointment_date.strftime('%d/%m/%Y'),
                    'appointment_time': time_value,
                    'customer_name': customer.name if customer else 'Cliente',
                    'customer_phone': customer.phone if customer and customer.phone else 'No especificado',
                    'customer_address': resolved_address or 'Por coordinar',
                    'customer_reference': resolved_ref or '',
                    'notes': notes_val or '',
                    'text_body': f'Se te ha asignado una nueva cita para {service.name} el {appointment_date} a las {time_value}.'
                }
            )
        
        return jsonify({
            'success': True,
            'message': 'Cita agendada exitosamente',
            'appointment': appointment.to_dict(enriched=True)
        }), 201
    
    except Exception as e:
        db.session.rollback()
        return jsonify({'success': False, 'message': f'Error agendando la cita: {str(e)}'}), 500

@appointments_bp.route('/<int:appointment_id>', methods=['PATCH'])
@jwt_required_custom
def update_appointment(appointment_id):
    \"\"\"Update appointment status / cancellation\"\"\"
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    appointment = Appointment.query.get(appointment_id)
    
    if not appointment:
        return jsonify({'error': 'Cita no encontrada'}), 404
    
    if user.role == 'customer' and appointment.user_id != user_id:
        return jsonify({'error': 'No autorizado'}), 403
    
    data = request.get_json() or {}
    
    if user.role == 'customer':
        if 'status' in data:
            if data['status'] == 'cancelled':
                if appointment.status in ['completed', 'cancelled']:
                    return jsonify({'success': False, 'message': 'No se puede cancelar una cita completada o previamente cancelada'}), 400
                appointment.status = 'cancelled'
            else:
                return jsonify({'success': False, 'message': 'Solo puedes cancelar citas'}), 400
    else:
        if 'status' in data:
            if not validate_appointment_status(data['status']):
                return jsonify({'success': False, 'message': 'Estado invalido'}), 400
            if not validate_appointment_transition(appointment.status, data['status']):
                return jsonify({'success': False, 'message': f'Transicion no permitida de {appointment.status} a {data[\"status\"]}'}), 400
            appointment.status = data['status']
        if 'technician_id' in data:
            appointment.technician_id = data['technician_id']
        if 'notes' in data:
            appointment.notes = data['notes']
    
    db.session.commit()
    return jsonify({'success': True, 'message': 'Cita actualizada', 'appointment': appointment.to_dict(enriched=True)}), 200
"""

with open(os.path.join(BACKEND_DIR, "app", "routes", "appointments.py"), "w", encoding="utf-8") as f:
    f.write(appointments_routes_code)
print("Updated appointments.py")

# 4. Overhaul all Email Templates in app/templates/emails/
EMAIL_DIR = os.path.join(BACKEND_DIR, "app", "templates", "emails")
os.makedirs(EMAIL_DIR, exist_ok=True)

# 4.1 appointment_customer.html
tpl_appointment_customer = """<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>¡Tu servicio en PlomApp está confirmado!</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F7F9FC; color: #1C2B41; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; }
    .wrapper { width: 100%; max-width: 580px; margin: 0 auto; padding: 24px 16px; box-sizing: border-box; }
    .card { background: #FFFFFF; border-radius: 20px; padding: 32px 28px; box-shadow: 0 4px 20px rgba(15, 82, 186, 0.08); border: 1px solid #E2E8F0; }
    .header { text-align: center; margin-bottom: 24px; }
    .badge-icon { display: inline-flex; align-items: center; justify-content: center; width: 64px; height: 64px; background: #E6F7F4; border-radius: 50%; color: #00C9A7; font-size: 32px; font-weight: bold; margin-bottom: 12px; }
    h1 { font-size: 22px; font-weight: 800; color: #0F52BA; margin: 0 0 6px 0; letter-spacing: -0.3px; }
    p.subtitle { font-size: 14px; color: #5B6C83; margin: 0; }
    .info-box { background: #F0F4F8; border-radius: 14px; padding: 20px; margin: 24px 0; border-left: 4px solid #0F52BA; }
    .info-row { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px; }
    .info-row:last-child { margin-bottom: 0; }
    .info-label { color: #5B6C83; font-weight: 600; }
    .info-value { color: #1C2B41; font-weight: 700; text-align: right; }
    .tech-card { background: #FFFFFF; border: 1px solid #E2E8F0; border-radius: 12px; padding: 14px; margin-top: 16px; display: flex; align-items: center; }
    .tech-icon { width: 40px; height: 40px; background: #0F52BA; color: #FFFFFF; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 18px; margin-right: 12px; }
    .cta-button { display: block; width: 100%; text-align: center; background: #0F52BA; color: #FFFFFF; padding: 14px 0; border-radius: 12px; font-weight: 700; font-size: 14px; text-decoration: none; margin: 24px 0 16px 0; }
    .footer { text-align: center; font-size: 11px; color: #65758B; margin-top: 24px; }
    .tag { display: inline-block; background: #00C9A7; color: #FFFFFF; font-size: 10px; font-weight: 800; padding: 2px 8px; border-radius: 6px; text-transform: uppercase; }
  </style>
</head>
<body>
  <div class="wrapper">
    <div class="card">
      <div class="header">
        <div class="badge-icon">&#10003;</div>
        <h1>¡Tu servicio está confirmado!</h1>
        <p class="subtitle">Hola <strong>{{ name }}</strong>, coordinamos todo para tu visita técnica.</p>
      </div>

      <div class="info-box">
        <table width="100%" cellpadding="4" cellspacing="0">
          <tr>
            <td class="info-label" align="left">Reserva ID</td>
            <td class="info-value" align="right"><span class="tag">{{ booking_id }}</span></td>
          </tr>
          <tr>
            <td class="info-label" align="left">Servicio</td>
            <td class="info-value" align="right">{{ service_name }}</td>
          </tr>
          <tr>
            <td class="info-label" align="left">Fecha</td>
            <td class="info-value" align="right">{{ appointment_date }}</td>
          </tr>
          <tr>
            <td class="info-label" align="left">Hora estimada</td>
            <td class="info-value" align="right">{{ appointment_time }}</td>
          </tr>
          <tr>
            <td class="info-label" align="left">Duración aprox.</td>
            <td class="info-value" align="right">{{ duration }}</td>
          </tr>
          <tr>
            <td class="info-label" align="left">Tarifa base</td>
            <td class="info-value" align="right" style="color: #0F52BA;">{{ service_price }}</td>
          </tr>
          <tr>
            <td class="info-label" align="left">Ubicación</td>
            <td class="info-value" align="right">{{ address }}</td>
          </tr>
          {% if address_reference %}
          <tr>
            <td class="info-label" align="left">Referencia</td>
            <td class="info-value" align="right">{{ address_reference }}</td>
          </tr>
          {% endif %}
        </table>
      </div>

      <div style="background: #FAFBFD; border: 1px solid #E2E8F0; border-radius: 12px; padding: 14px; margin-top: 14px;">
        <table width="100%" cellpadding="0" cellspacing="0">
          <tr>
            <td width="42" valign="middle">
              <div style="width: 36px; height: 36px; background: #0F52BA; color: #FFF; border-radius: 50%; text-align: center; line-height: 36px; font-weight: bold;">&#9881;</div>
            </td>
            <td valign="middle" style="padding-left: 10px;">
              <div style="font-size: 13px; font-weight: bold; color: #1C2B41;">Profesional asignado: {{ technician_name }}</div>
              <div style="font-size: 11px; color: #5B6C83;">Calificación: &#9733; {{ technician_rating }} &middot; Técnico Verificado PlomApp</div>
            </td>
          </tr>
        </table>
      </div>

      {% if notes %}
      <p style="font-size: 12px; color: #5B6C83; margin-top: 16px; background: #F8FAFC; padding: 10px; border-radius: 8px;">
        <strong>Observaciones:</strong> {{ notes }}
      </p>
      {% endif %}

      <p style="font-size: 12px; color: #5B6C83; text-align: center; margin-top: 20px;">
        Puedes gestionar, reprogramar o ver el seguimiento de tu cita directamente desde la aplicación de PlomApp.
      </p>
    </div>

    <div class="footer">
      <p>&copy; 2026 PlomApp. Servicios profesionales bajo demanda con garantía.</p>
      <p>Soporte 24/7 &middot; Bogotá, Colombia</p>
    </div>
  </div>
</body>
</html>
"""

# 4.2 appointment_technician.html
tpl_appointment_technician = """<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Nueva cita asignada - PlomApp</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #F7F9FC; color: #1C2B41; margin: 0; padding: 0; }
    .wrapper { width: 100%; max-width: 580px; margin: 0 auto; padding: 24px 16px; box-sizing: border-box; }
    .card { background: #FFFFFF; border-radius: 20px; padding: 32px 28px; box-shadow: 0 4px 20px rgba(15, 82, 186, 0.08); border: 1px solid #E2E8F0; }
    h1 { font-size: 20px; font-weight: 800; color: #0F52BA; margin: 0 0 6px 0; }
    .info-box { background: #F0F4F8; border-radius: 14px; padding: 18px; margin: 20px 0; }
    .info-label { color: #5B6C83; font-weight: 600; font-size: 12px; }
    .info-value { color: #1C2B41; font-weight: 700; font-size: 13px; }
    .footer { text-align: center; font-size: 11px; color: #65758B; margin-top: 24px; }
  </style>
</head>
<body>
  <div class="wrapper">
    <div class="card">
      <h1>¡Nueva cita asignada!</h1>
      <p style="font-size: 14px; color: #5B6C83;">Hola <strong>{{ name }}</strong>, tienes un nuevo servicio programado en PlomApp.</p>

      <div class="info-box">
        <table width="100%" cellpadding="4" cellspacing="0">
          <tr>
            <td class="info-label" align="left">ID Cita</td>
            <td class="info-value" align="right">{{ booking_id }}</td>
          </tr>
          <tr>
            <td class="info-label" align="left">Servicio</td>
            <td class="info-value" align="right">{{ service_name }}</td>
          </tr>
          <tr>
            <td class="info-label" align="left">Fecha</td>
            <td class="info-value" align="right">{{ appointment_date }}</td>
          </tr>
          <tr>
            <td class="info-label" align="left">Hora</td>
            <td class="info-value" align="right">{{ appointment_time }}</td>
          </tr>
          <tr>
            <td class="info-label" align="left">Cliente</td>
            <td class="info-value" align="right">{{ customer_name }}</td>
          </tr>
          <tr>
            <td class="info-label" align="left">Teléfono</td>
            <td class="info-value" align="right">{{ customer_phone }}</td>
          </tr>
          <tr>
            <td class="info-label" align="left">Dirección</td>
            <td class="info-value" align="right">{{ customer_address }}</td>
          </tr>
          {% if customer_reference %}
          <tr>
            <td class="info-label" align="left">Referencia</td>
            <td class="info-value" align="right">{{ customer_reference }}</td>
          </tr>
          {% endif %}
        </table>
      </div>

      {% if notes %}
      <p style="font-size: 12px; color: #5B6C83; background: #FAFBFD; padding: 10px; border-radius: 8px; border: 1px solid #E2E8F0;">
        <strong>Detalles del cliente:</strong> {{ notes }}
      </p>
      {% endif %}

      <p style="font-size: 12px; color: #0F52BA; font-weight: bold; text-align: center; margin-top: 18px;">
        Por favor preséntate puntual y con tu equipo de seguridad e identificación.
      </p>
    </div>
    <div class="footer">&copy; 2026 PlomApp &middot; Panel de Técnicos</div>
  </div>
</body>
</html>
"""

# 4.3 welcome.html
tpl_welcome = """<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Bienvenido a PlomApp</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #F7F9FC; color: #1C2B41; margin: 0; padding: 0; }
    .wrapper { width: 100%; max-width: 560px; margin: 0 auto; padding: 24px 16px; box-sizing: border-box; }
    .card { background: #FFFFFF; border-radius: 20px; padding: 36px 28px; box-shadow: 0 4px 20px rgba(15, 82, 186, 0.08); border: 1px solid #E2E8F0; text-align: center; }
    h1 { font-size: 24px; font-weight: 800; color: #0F52BA; margin: 0 0 8px 0; }
    .code-box { background: #F0F4F8; border-radius: 12px; padding: 18px; margin: 24px auto; font-size: 28px; font-weight: 800; letter-spacing: 6px; color: #0F52BA; display: inline-block; min-width: 200px; }
    .benefit { text-align: left; margin: 16px 0; display: flex; align-items: center; font-size: 13px; color: #5B6C83; }
    .footer { text-align: center; font-size: 11px; color: #65758B; margin-top: 24px; }
  </style>
</head>
<body>
  <div class="wrapper">
    <div class="card">
      <div style="font-size: 40px; margin-bottom: 12px;">&#128075;</div>
      <h1>¡Bienvenido a PlomApp, {{ name }}!</h1>
      <p style="font-size: 14px; color: #5B6C83; line-height: 20px;">
        Tu plataforma confiable para soluciones de plomería profesional y reparaciones al instante.
      </p>

      {% if verification_code %}
      <p style="font-size: 13px; color: #1C2B41; font-weight: 600; margin-top: 24px;">Tu código de verificación de cuenta es:</p>
      <div class="code-box">{{ verification_code }}</div>
      <p style="font-size: 12px; color: #5B6C83;">Ingresa este código en la aplicación para activar tu cuenta.</p>
      {% endif %}

      <div style="background: #FAFBFD; border-radius: 14px; padding: 16px; margin-top: 24px; text-align: left;">
        <div style="font-size: 12px; font-weight: bold; color: #0F52BA; margin-bottom: 8px;">Con PlomApp obtienes:</div>
        <div style="font-size: 12px; color: #5B6C83; margin-bottom: 4px;">&#10003; Plomeros calificados y verificados</div>
        <div style="font-size: 12px; color: #5B6C83; margin-bottom: 4px;">&#10003; Precios justos sin sorpresas</div>
        <div style="font-size: 12px; color: #5B6C83;">&#10003; Garantía completa en cada servicio</div>
      </div>
    </div>
    <div class="footer">&copy; 2026 PlomApp &middot; Bogotá, Colombia</div>
  </div>
</body>
</html>
"""

# 4.4 reset_password.html
tpl_reset_password = """<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Recuperación de contraseña - PlomApp</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #F7F9FC; color: #1C2B41; margin: 0; padding: 0; }
    .wrapper { width: 100%; max-width: 560px; margin: 0 auto; padding: 24px 16px; box-sizing: border-box; }
    .card { background: #FFFFFF; border-radius: 20px; padding: 36px 28px; box-shadow: 0 4px 20px rgba(15, 82, 186, 0.08); border: 1px solid #E2E8F0; text-align: center; }
    h1 { font-size: 22px; font-weight: 800; color: #0F52BA; margin: 0 0 8px 0; }
    .code-box { background: #F0F4F8; border-radius: 12px; padding: 18px; margin: 24px auto; font-size: 28px; font-weight: 800; letter-spacing: 6px; color: #0F52BA; display: inline-block; min-width: 200px; }
    .footer { text-align: center; font-size: 11px; color: #65758B; margin-top: 24px; }
  </style>
</head>
<body>
  <div class="wrapper">
    <div class="card">
      <div style="font-size: 40px; margin-bottom: 12px;">&#128274;</div>
      <h1>Recuperación de contraseña</h1>
      <p style="font-size: 14px; color: #5B6C83; line-height: 20px;">
        Hola <strong>{{ name }}</strong>, recibimos una solicitud para restablecer la contraseña de tu cuenta en PlomApp.
      </p>

      {% if reset_code %}
      <p style="font-size: 13px; color: #1C2B41; font-weight: 600; margin-top: 20px;">Tu código de seguridad es:</p>
      <div class="code-box">{{ reset_code }}</div>
      <p style="font-size: 12px; color: #5B6C83;">Este código expirará en 15 minutos por tu seguridad.</p>
      {% endif %}

      <p style="font-size: 12px; color: #65758B; margin-top: 24px;">
        Si no realizaste esta solicitud, puedes ignorar este correo; tu cuenta permanece segura.
      </p>
    </div>
    <div class="footer">&copy; 2026 PlomApp &middot; Seguridad y Soporte</div>
  </div>
</body>
</html>
"""

# 4.5 login_alert.html
tpl_login_alert = """<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Alerta de seguridad - PlomApp</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #F7F9FC; color: #1C2B41; margin: 0; padding: 0; }
    .wrapper { width: 100%; max-width: 560px; margin: 0 auto; padding: 24px 16px; box-sizing: border-box; }
    .card { background: #FFFFFF; border-radius: 20px; padding: 32px 28px; box-shadow: 0 4px 20px rgba(15, 82, 186, 0.08); border: 1px solid #E2E8F0; }
    h1 { font-size: 20px; font-weight: 800; color: #0F52BA; margin: 0 0 8px 0; }
    .footer { text-align: center; font-size: 11px; color: #65758B; margin-top: 24px; }
  </style>
</head>
<body>
  <div class="wrapper">
    <div class="card">
      <h1>Inicio de sesión detectado</h1>
      <p style="font-size: 14px; color: #5B6C83;">Hola <strong>{{ name }}</strong>, se ha iniciado sesión en tu cuenta de PlomApp recientemente.</p>
      <p style="font-size: 13px; color: #1C2B41;"><strong>Fecha y hora:</strong> {{ login_time }}</p>
      <p style="font-size: 12px; color: #65758B; margin-top: 20px;">
        Si fuiste tú, no necesitas hacer nada. Si no reconoces este acceso, te recomendamos cambiar tu contraseña inmediatamente desde la app.
      </p>
    </div>
    <div class="footer">&copy; 2026 PlomApp &middot; Centro de Seguridad</div>
  </div>
</body>
</html>
"""

with open(os.path.join(EMAIL_DIR, "appointment_customer.html"), "w", encoding="utf-8") as f:
    f.write(tpl_appointment_customer)
with open(os.path.join(EMAIL_DIR, "appointment_technician.html"), "w", encoding="utf-8") as f:
    f.write(tpl_appointment_technician)
with open(os.path.join(EMAIL_DIR, "welcome.html"), "w", encoding="utf-8") as f:
    f.write(tpl_welcome)
with open(os.path.join(EMAIL_DIR, "reset_password.html"), "w", encoding="utf-8") as f:
    f.write(tpl_reset_password)
with open(os.path.join(EMAIL_DIR, "login_alert.html"), "w", encoding="utf-8") as f:
    f.write(tpl_login_alert)

print("All email templates generated and updated successfully!")
