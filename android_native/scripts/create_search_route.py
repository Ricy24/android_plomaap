import os
import sys

TARGET_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\search.py"

content = '''"""
Hybrid Search Blueprint for PlomApp
FASE 10: Búsqueda Híbrida Inteligente (Secciones 14, 17, 18 y 19 del Plan Maestro)
Endpoints:
  POST /api/search/hybrid  - Búsqueda híbrida de servicios (léxica + semántica vectorial)
"""
import logging
from flask import Blueprint, jsonify, request
from app.services.hybrid_search_service import perform_hybrid_search

logger = logging.getLogger(__name__)

search_bp = Blueprint('search', __name__, url_prefix='/api/search')


@search_bp.route('/hybrid', methods=['POST'])
def search_hybrid():
    """
    POST /api/search/hybrid
    Acepta:
      {
        "query": "se me está saliendo agua debajo del lavamanos",
        "top_k": 5,
        "category": "Fugas" (opcional)
      }
    Retorna:
      Lista de servicios reales clasificados por hybrid_score (RRF ponderado)
      con metadatos de coincidencia, activos detectados y explicaciones.
    """
    data = request.get_json() or {}
    query = data.get('query', '').strip()
    top_k = int(data.get('top_k', 5))
    category = data.get('category')

    if top_k > 20:
        top_k = 20
    if top_k < 1:
        top_k = 5

    try:
        results = perform_hybrid_search(
            query=query,
            category_filter=category,
            top_k=top_k
        )
        return jsonify({
            'success': True,
            **results
        }), 200
    except Exception as e:
        logger.error(f"Error ejecutando búsqueda híbrida: {e}", exc_info=True)
        return jsonify({
            'success': False,
            'error': 'Error procesando búsqueda híbrida',
            'details': str(e)
        }), 500
'''

with open(TARGET_PATH, "w", encoding="utf-8") as f:
    f.write(content.strip() + "\n")

print(f"[OK] search.py generado exitosamente en {TARGET_PATH}")
