import os

TARGET_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\intelligence.py"

content = '''"""
Intelligence Blueprint for PlomApp
FASE 11: Comprensión de Lenguaje Natural y Extracción de Intenciones
(Secciones 15, 17, 18 y 19 del Plan Maestro)

Endpoints:
  POST /api/intelligence/understand  - Analiza texto en lenguaje natural y extrae intención, activos, urgencia y servicios recomendados.
"""
import logging
from flask import Blueprint, jsonify, request
from app.services.nlu_service import understand_text

logger = logging.getLogger(__name__)

intelligence_bp = Blueprint('intelligence', __name__, url_prefix='/api/intelligence')


@intelligence_bp.route('/understand', methods=['POST'])
def process_text_understanding():
    """
    POST /api/intelligence/understand
    Acepta:
      {
        "text": "se me está saliendo agua por debajo del lavamanos"
      }
    Retorna:
      Estructura NLU validada con IDs reales de PostgreSQL y recomendación de servicios.
    """
    data = request.get_json() or {}
    text = data.get('text', '').strip()

    if not text:
        return jsonify({
            'success': False,
            'error': 'El campo text es obligatorio'
        }), 400

    user_id = None
    try:
        from flask_jwt_extended import verify_jwt_in_request, get_jwt_identity
        verify_jwt_in_request(optional=True)
        jwt_id = get_jwt_identity()
        if jwt_id:
            user_id = int(jwt_id)
    except Exception:
        pass

    try:
        result = understand_text(text=text, user_id=user_id)
        status_code = 200 if result.get('success') else 400
        return jsonify(result), status_code
    except Exception as e:
        logger.error(f"Error procesando comprensión NLU: {e}", exc_info=True)
        return jsonify({
            'success': False,
            'error': 'Error interno procesando análisis NLU',
            'details': str(e)
        }), 500
'''

with open(TARGET_PATH, "w", encoding="utf-8") as f:
    f.write(content.strip() + "\n")

print(f"[OK] intelligence.py corregido exitosamente en {TARGET_PATH}")
