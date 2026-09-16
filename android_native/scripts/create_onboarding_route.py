import os
import sys

TARGET_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\onboarding.py"

content = '''"""
Adaptive Onboarding Routes for PlomApp
FASE 8: Onboarding Inteligente
Endpoints:
  GET  /api/onboarding/status     - Consulta el estado de onboarding del usuario
  GET  /api/onboarding/questions  - Retorna las preguntas adaptativas
  POST /api/onboarding/start      - Inicia el flujo de onboarding (evento ONBOARDING_STARTED)
  POST /api/onboarding/complete   - Guarda respuestas y aprovisiona hogar (evento ONBOARDING_COMPLETED)
  POST /api/onboarding/skip       - Omite el onboarding ("Ahora no")
"""
import logging
from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity

from app.services.onboarding_service import (
    get_adaptive_questions,
    get_user_onboarding_status,
    start_onboarding,
    skip_onboarding,
    complete_onboarding
)

logger = logging.getLogger(__name__)

onboarding_bp = Blueprint('onboarding', __name__, url_prefix='/api/onboarding')


@onboarding_bp.route('/status', methods=['GET'])
@jwt_required()
def get_status():
    """
    GET /api/onboarding/status
    Devuelve el estado de onboarding del usuario autenticado.
    """
    user_id = int(get_jwt_identity())
    status = get_user_onboarding_status(user_id)
    return jsonify({
        'success': True,
        'user_id': user_id,
        'status': status
    }), 200


@onboarding_bp.route('/questions', methods=['GET'])
@jwt_required(optional=True)
def get_questions():
    """
    GET /api/onboarding/questions
    Retorna el árbol de preguntas adaptativas.
    Parámetro opcional: ?home_type=apartment|house|other
    """
    home_type = request.args.get('home_type')
    questions_data = get_adaptive_questions(home_type)
    return jsonify({
        'success': True,
        'data': questions_data
    }), 200


@onboarding_bp.route('/start', methods=['POST'])
@jwt_required()
def start():
    """
    POST /api/onboarding/start
    Inicia la sesión de onboarding para el usuario autenticado y registra ONBOARDING_STARTED.
    """
    user_id = int(get_jwt_identity())
    data = request.get_json() or {}
    session_id = data.get('session_id')
    
    questions_data = start_onboarding(user_id, session_id=session_id)
    return jsonify({
        'success': True,
        'message': 'Sesión de onboarding iniciada',
        'data': questions_data
    }), 200


@onboarding_bp.route('/complete', methods=['POST'])
@jwt_required()
def complete():
    """
    POST /api/onboarding/complete
    Procesa las 5 respuestas adaptativas, aprovisiona el hogar, habitaciones y activos,
    y registra procedencia en profile_attributes ('source=onboarding') y evento ONBOARDING_COMPLETED.
    """
    user_id = int(get_jwt_identity())
    data = request.get_json() or {}
    
    # Soportar payload anidado {"answers": {...}} o plano {home_type: ..., key_zones: ...}
    answers = data.get('answers') if 'answers' in data else data
    session_id = data.get('session_id')
    
    try:
        result = complete_onboarding(user_id, answers, session_id=session_id)
        return jsonify(result), 200
    except Exception as e:
        logger.error(f"Error completando onboarding para usuario #{user_id}: {e}", exc_info=True)
        return jsonify({
            'success': False,
            'error': 'Error interno al procesar respuestas de onboarding'
        }), 500


@onboarding_bp.route('/skip', methods=['POST'])
@jwt_required()
def skip():
    """
    POST /api/onboarding/skip
    Registra la acción 'Ahora no' para posponer el onboarding sin bloquear el sistema.
    """
    user_id = int(get_jwt_identity())
    data = request.get_json() or {}
    session_id = data.get('session_id')
    
    result = skip_onboarding(user_id, session_id=session_id)
    return jsonify(result), 200
'''

with open(TARGET_PATH, "w", encoding="utf-8") as f:
    f.write(content.strip() + "\n")

print(f"[OK] onboarding.py generado exitosamente en {TARGET_PATH}")
