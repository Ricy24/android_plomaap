import os
import shutil
import re

TARGET = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\appointments.py"
BACKUP = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\appointments.py.bak_fase6"

shutil.copy2(TARGET, BACKUP)
print(f"[OK] Backup creado en {BACKUP}")

with open(TARGET, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add import
assert 'from app.services.event_service import track_event' not in content
import_line = "from app.services.event_service import track_event\n"
content = import_line + content

# 2. Add BOOKING_CREATED inside create_appointment()
# Search for return jsonify with 'Cita agendada exitosamente'
pattern_create = r"(\s+)(return jsonify\({\s+'success': True,\s+'message': 'Cita agendada exitosamente',)"
match_create = re.search(pattern_create, content)
assert match_create is not None, "create_appointment return not found"

booking_created_code = """        # FASE 6: Event Tracking
        track_event(
            event_type='BOOKING_CREATED',
            user_id=user_id,
            entity_type='appointment',
            entity_id=appointment.id,
            metadata={
                'service_id': service.id,
                'service_name': service.name,
                'technician_id': appointment.technician_id,
                'date': str(appointment.date),
                'time': str(appointment.time),
                'total_price': float(total_price)
            }
        )
"""

content = content[:match_create.start(2)] + booking_created_code + match_create.group(1) + content[match_create.start(2):]

# 3. Add BOOKING_CANCELLED and BOOKING_COMPLETED inside update_appointment()
# In update_appointment:
# Before updating appointment.status, record old_status
# After db.session.commit(), track event if status changed to cancelled or completed

# Look for user_id = get_jwt_identity() ... appointment = Appointment.query.get(appointment_id)
old_status_marker = "    appointment = Appointment.query.get(appointment_id)"
old_status_repl = "    appointment = Appointment.query.get(appointment_id)\n    old_status = appointment.status if appointment else None"
assert old_status_marker in content, "old_status_marker not found"
content = content.replace(old_status_marker, old_status_repl, 1)

# Look for db.session.commit() at the end of update_appointment
pattern_update = r"(db\.session\.commit\(\)\s+)(return jsonify\({\s*'success': True,\s*'message': 'Cita actualizada',)"
match_update = re.search(pattern_update, content)
assert match_update is not None, "update_appointment return not found"

status_event_code = """
    # FASE 6: Event Tracking
    if appointment.status != old_status:
        if appointment.status == 'cancelled':
            track_event(
                event_type='BOOKING_CANCELLED',
                user_id=user_id,
                entity_type='appointment',
                entity_id=appointment.id,
                metadata={
                    'previous_status': old_status,
                    'cancelled_by': user.role if user else 'unknown',
                    'reason': data.get('reason') or data.get('cancellation_reason')
                }
            )
        elif appointment.status == 'completed':
            track_event(
                event_type='BOOKING_COMPLETED',
                user_id=user_id,
                entity_type='appointment',
                entity_id=appointment.id,
                metadata={
                    'previous_status': old_status,
                    'completed_by': user.role if user else 'unknown'
                }
            )
    """

content = content[:match_update.start(2)] + status_event_code + "\n    " + content[match_update.start(2):]

with open(TARGET, 'w', encoding='utf-8') as f:
    f.write(content)

print("[OK] BOOKING_CREATED, BOOKING_CANCELLED y BOOKING_COMPLETED instrumentados en appointments.py exitosamente.")
