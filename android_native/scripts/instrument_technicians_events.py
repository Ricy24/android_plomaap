import os
import shutil
import re

TARGET = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\technicians.py"
BACKUP = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\technicians.py.bak_fase6"

shutil.copy2(TARGET, BACKUP)
print(f"[OK] Backup creado en {BACKUP}")

with open(TARGET, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add import
if 'from app.services.event_service import track_event' not in content:
    content = "from app.services.event_service import track_event\n" + content

# 2. Add old_status capture
old_status_marker = "    appointment = Appointment.query.get(appointment_id)"
old_status_repl = "    appointment = Appointment.query.get(appointment_id)\n    old_status = appointment.status if appointment else None"
assert old_status_marker in content, "old_status_marker not found"
content = content.replace(old_status_marker, old_status_repl, 1)

# 3. Add tracking after commit
target_block = """    db.session.commit()
    return jsonify(appointment.to_dict(enriched=True)), 200"""

replacement_block = """    db.session.commit()
    
    # FASE 6: Event Tracking
    if 'status' in data and data['status'] != old_status:
        if data['status'] == 'cancelled':
            track_event(
                event_type='BOOKING_CANCELLED',
                user_id=user_id,
                entity_type='appointment',
                entity_id=appointment.id,
                metadata={
                    'previous_status': old_status,
                    'cancelled_by': 'technician',
                    'reason': data.get('reason')
                }
            )
        elif data['status'] == 'completed':
            track_event(
                event_type='BOOKING_COMPLETED',
                user_id=user_id,
                entity_type='appointment',
                entity_id=appointment.id,
                metadata={
                    'previous_status': old_status,
                    'completed_by': 'technician'
                }
            )

    return jsonify(appointment.to_dict(enriched=True)), 200"""

assert target_block in content, "Target block in technicians.py not found"
content = content.replace(target_block, replacement_block, 1)

with open(TARGET, 'w', encoding='utf-8') as f:
    f.write(content)

print("[OK] Eventos instrumentados en technicians.py exitosamente.")
