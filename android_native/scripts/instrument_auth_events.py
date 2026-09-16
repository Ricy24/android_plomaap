import os
import shutil

TARGET = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\controllers\auth_controller.py"
BACKUP = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\controllers\auth_controller.py.bak_fase6"

# Restore from backup first if modified
if os.path.exists(BACKUP):
    shutil.copy2(BACKUP, TARGET)

with open(TARGET, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add import
if 'from app.services.event_service import track_event' not in content:
    content = "from app.services.event_service import track_event\n" + content

# 2. Add track_event before return jsonify in register()
target_block = """    send_template_email(
        user.email,
        'Verifica tu cuenta en PlomApp',
        'welcome',
        {
            'name': user.name,
            'role': user.role,
            'role_label': 'Cliente' if user.role == 'customer' else 'Técnico' if user.role == 'technician' else 'Administrador',
            'text_body': 'Gracias por registrarte en PlomApp.'
        }
    )
    return jsonify({
        'success': True,
        'message': 'Registro exitoso',"""

# Note the potential accent or encoding in 'Técnico'
# Let's match more flexibly
import re

pattern = r"(\s+send_template_email\(\s+user\.email,\s+'Verifica tu cuenta en PlomApp'.*?\)\s+)(return jsonify\({\s+'success': True,\s+'message': 'Registro exitoso',)"

match = re.search(pattern, content, re.DOTALL)
assert match is not None, "Regex pattern not matched in auth_controller.py"

event_code = """    # FASE 6: Event Tracking
    track_event(
        event_type='USER_REGISTERED',
        user_id=user.id,
        entity_type='user',
        entity_id=user.id,
        metadata={
            'email': user.email,
            'role': user.role,
            'name': user.name
        }
    )
    """

new_content = content[:match.start(2)] + event_code + content[match.start(2):]

with open(TARGET, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("[OK] USER_REGISTERED instrumentado en auth_controller.py exitosamente.")
