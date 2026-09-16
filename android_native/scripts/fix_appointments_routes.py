TARGET = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\appointments.py"

with open(TARGET, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Clean get_appointment
target_get = """@appointments_bp.route('/<int:appointment_id>', methods=['GET'])
@jwt_required_custom
def get_appointment(appointment_id):
    \"\"\"Get single appointment details\"\"\"
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    appointment = Appointment.query.get(appointment_id)
    old_status = appointment.status if appointment else None
    
    if not appointment:
        return jsonify({'error': 'Appointment not found'}), 404
    
    if user.role == 'customer' and appointment.user_id != user_id:
        return jsonify({'error': 'Unauthorized'}), 403
    elif user.role == 'technician' and appointment.technician_id != user_id:
        return jsonify({'error': 'Unauthorized'}), 403"""

repl_get = """@appointments_bp.route('/<int:appointment_id>', methods=['GET'])
@jwt_required_custom
def get_appointment(appointment_id):
    \"\"\"Get single appointment details\"\"\"
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    appointment = Appointment.query.get(appointment_id)
    
    if not appointment:
        return jsonify({'error': 'Appointment not found'}), 404
    
    if user.role == 'customer' and appointment.user_id != int(user_id):
        return jsonify({'error': 'Unauthorized'}), 403
    elif user.role == 'technician' and appointment.technician_id != int(user_id):
        return jsonify({'error': 'Unauthorized'}), 403"""

assert target_get in content, "target_get not found"
content = content.replace(target_get, repl_get, 1)

# 2. Fix update_appointment
target_update = """@appointments_bp.route('/<int:appointment_id>', methods=['PATCH'])
@jwt_required_custom
def update_appointment(appointment_id):
    \"\"\"Update appointment status / cancellation\"\"\"
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    appointment = Appointment.query.get(appointment_id)
    
    if not appointment:
        return jsonify({'error': 'Cita no encontrada'}), 404
    
    if user.role == 'customer' and appointment.user_id != user_id:
        return jsonify({'error': 'No autorizado'}), 403"""

repl_update = """@appointments_bp.route('/<int:appointment_id>', methods=['PATCH'])
@jwt_required_custom
def update_appointment(appointment_id):
    \"\"\"Update appointment status / cancellation\"\"\"
    user_id = get_jwt_identity()
    user = User.query.get(user_id)
    appointment = Appointment.query.get(appointment_id)
    
    if not appointment:
        return jsonify({'error': 'Cita no encontrada'}), 404
    
    old_status = appointment.status
    if user.role == 'customer' and appointment.user_id != int(user_id):
        return jsonify({'error': 'No autorizado'}), 403"""

assert target_update in content, "target_update not found"
content = content.replace(target_update, repl_update, 1)

with open(TARGET, 'w', encoding='utf-8') as f:
    f.write(content)

print("[OK] appointments.py corregido con int(user_id) y old_status en update_appointment.")
