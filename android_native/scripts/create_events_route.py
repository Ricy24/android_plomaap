import os
import shutil

ROUTE_FILE = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\events.py"
INIT_FILE = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\__init__.py"
INIT_BAK = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\__init__.py.bak_fase6"

# 1. Create app/routes/events.py
route_code = '''from flask import Blueprint, jsonify, request
from flask_jwt_extended import verify_jwt_in_request, get_jwt_identity
from app.services.event_service import track_event, VALID_EVENT_TYPES

events_bp = Blueprint('events', __name__, url_prefix='/api/events')

@events_bp.route('', methods=['POST'])
def create_event():
    """
    POST /api/events
    Endpoint genérico para registro de eventos desde clientes (Android/Web).
    """
    data = request.get_json() or {}
    event_type = data.get('event_type')
    
    if not event_type:
        return jsonify({'success': False, 'message': 'El campo event_type es obligatorio'}), 400
    
    if event_type not in VALID_EVENT_TYPES:
        return jsonify({
            'success': False,
            'message': f'Tipo de evento inválido: {event_type}. Consulte el catálogo permitido.'
        }), 400
    
    # Resolver user_id desde JWT si está presente, o del payload
    resolved_user_id = None
    try:
        verify_jwt_in_request(optional=True)
        identity = get_jwt_identity()
        if identity:
            resolved_user_id = int(identity)
    except Exception:
        pass
    
    if resolved_user_id is None and data.get('user_id'):
        try:
            resolved_user_id = int(data.get('user_id'))
        except (ValueError, TypeError):
            pass
            
    entity_type = data.get('entity_type')
    entity_id = data.get('entity_id')
    if entity_id is not None:
        try:
            entity_id = int(entity_id)
        except (ValueError, TypeError):
            entity_id = None

    session_id = data.get('session_id') or request.headers.get('X-Session-ID')
    metadata = data.get('metadata') if isinstance(data.get('metadata'), dict) else {}

    event = track_event(
        event_type=event_type,
        user_id=resolved_user_id,
        entity_type=entity_type,
        entity_id=entity_id,
        session_id=session_id,
        metadata=metadata,
        strict=True
    )
    
    return jsonify({
        'success': True,
        'message': 'Evento registrado exitosamente',
        'event': event.to_dict()
    }), 201
'''

with open(ROUTE_FILE, 'w', encoding='utf-8') as f:
    f.write(route_code.strip() + "\n")
print(f"[OK] Creado {ROUTE_FILE}")

# 2. Register blueprint in app/__init__.py
shutil.copy2(INIT_FILE, INIT_BAK)
print(f"[OK] Backup creado en {INIT_BAK}")

with open(INIT_FILE, 'r', encoding='utf-8') as f:
    init_content = f.read()

assert 'from app.routes.events import events_bp' not in init_content

import_target = "from app.routes.favorites import favorites_bp"
import_replacement = "from app.routes.favorites import favorites_bp\nfrom app.routes.events import events_bp"

register_target = "    app.register_blueprint(favorites_bp)"
register_replacement = "    app.register_blueprint(favorites_bp)\n    app.register_blueprint(events_bp)"

assert import_target in init_content, "Import target not found"
assert register_target in init_content, "Register target not found"

new_init = init_content.replace(import_target, import_replacement).replace(register_target, register_replacement)

with open(INIT_FILE, 'w', encoding='utf-8') as f:
    f.write(new_init)

print("[OK] Blueprint events_bp registrado en app/__init__.py exitosamente.")
