import os

content = """from flask import Blueprint, jsonify, request
from app.utils.auth import token_required
from app.services.recommendation_service import (
    generate_recommendations,
    track_recommendation_click,
    track_recommendation_conversion
)
import logging

logger = logging.getLogger(__name__)

recommendations_bp = Blueprint('recommendations', __name__)

@recommendations_bp.route('/', methods=['GET'])
@token_required
def get_recommendations(current_user):
    \"\"\"
    GET /api/recommendations
    Retorna servicios recomendados para el usuario actual basándose en su Hogar Digital, Preferencias e Historial.
    \"\"\"
    try:
        limit = request.args.get('limit', 5, type=int)
        recommendations = generate_recommendations(user_id=current_user.id, limit=limit)
        
        return jsonify({
            'status': 'success',
            'data': recommendations
        }), 200
        
    except Exception as e:
        logger.error(f"Error fetching recommendations: {e}", exc_info=True)
        return jsonify({'status': 'error', 'message': 'Internal server error'}), 500


@recommendations_bp.route('/<int:rec_id>/click', methods=['POST'])
@token_required
def track_click(current_user, rec_id):
    \"\"\"
    POST /api/recommendations/<int:rec_id>/click
    Registra que el usuario interactuó con la recomendación sugerida.
    \"\"\"
    success = track_recommendation_click(rec_id, current_user.id)
    if success:
        return jsonify({'status': 'success', 'message': 'Click tracked successfully'}), 200
    else:
        # We can return 200 even if false, as it's just analytics, but let's be accurate.
        return jsonify({'status': 'error', 'message': 'Could not track click or recommendation not found'}), 400


@recommendations_bp.route('/<int:rec_id>/convert', methods=['POST'])
@token_required
def track_conversion(current_user, rec_id):
    \"\"\"
    POST /api/recommendations/<int:rec_id>/convert
    Registra que el usuario reservó el servicio sugerido a partir de la recomendación.
    \"\"\"
    success = track_recommendation_conversion(rec_id, current_user.id)
    if success:
        return jsonify({'status': 'success', 'message': 'Conversion tracked successfully'}), 200
    else:
        return jsonify({'status': 'error', 'message': 'Could not track conversion or recommendation not found'}), 400
"""

target = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\recommendations.py"
with open(target, 'w', encoding='utf-8') as f:
    f.write(content)
print("Created recommendations.py")
