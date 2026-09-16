import os
import sys

TARGET_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\embeddings.py"

content = '''"""
Embedding and Vector Search Routes for PlomApp
FASE 9: Embedding Provider
Endpoints:
  GET  /api/embeddings/provider-info  - Metadatos del proveedor de embeddings activo
  POST /api/embeddings/embed          - Genera y retorna el embedding de un texto
  POST /api/embeddings/sync           - Sincroniza catálogo de servicios y activos en pgvector
  POST /api/embeddings/search         - Búsqueda semántica pura por similitud vectorial
"""
import logging
from flask import Blueprint, jsonify, request
from app.services.embedding_provider import get_embedding_provider
from app.services.embedding_service import (
    store_entity_embedding,
    search_similar_entities,
    sync_catalog_embeddings
)

logger = logging.getLogger(__name__)

embeddings_bp = Blueprint('embeddings', __name__, url_prefix='/api/embeddings')


@embeddings_bp.route('/provider-info', methods=['GET'])
def get_provider_info():
    """
    GET /api/embeddings/provider-info
    Retorna el proveedor activo, modelo y dimensión vectorial.
    """
    prov = get_embedding_provider()
    return jsonify({
        'success': True,
        'provider_class': prov.__class__.__name__,
        'model_name': prov.model_name,
        'dimension': prov.dimension
    }), 200


@embeddings_bp.route('/embed', methods=['POST'])
def generate_embedding():
    """
    POST /api/embeddings/embed
    Genera embedding para un texto provisto.
    """
    data = request.get_json() or {}
    text_input = data.get('text')
    if not text_input or not str(text_input).strip():
        return jsonify({'error': 'El campo text es obligatorio'}), 400
        
    prov = get_embedding_provider()
    try:
        vec = prov.embed_text(str(text_input).strip())
        return jsonify({
            'success': True,
            'model_name': prov.model_name,
            'dimension': len(vec),
            'embedding_sample': vec[:8],  # Muestra inicial para inspección
            'embedding': vec if data.get('include_full_vector') else None
        }), 200
    except Exception as e:
        logger.error(f"Error generando embedding: {e}", exc_info=True)
        return jsonify({'error': f'Error generando embedding: {str(e)}'}), 500


@embeddings_bp.route('/sync', methods=['POST'])
def sync_embeddings():
    """
    POST /api/embeddings/sync
    Genera y almacena en pgvector los embeddings de servicios y activos.
    """
    try:
        result = sync_catalog_embeddings(commit=True)
        return jsonify({
            'success': True,
            'message': 'Catálogo sincronizado exitosamente en pgvector',
            'data': result
        }), 200
    except Exception as e:
        logger.error(f"Error sincronizando catálogo: {e}", exc_info=True)
        return jsonify({'error': f'Error sincronizando catálogo: {str(e)}'}), 500


@embeddings_bp.route('/search', methods=['POST'])
def search_embeddings():
    """
    POST /api/embeddings/search
    Realiza búsqueda vectorial por similitud semántica.
    """
    data = request.get_json() or {}
    query_text = data.get('query')
    if not query_text or not str(query_text).strip():
        return jsonify({'error': 'El campo query es obligatorio'}), 400
        
    entity_type = data.get('entity_type')  # 'service' | 'asset_type' | None
    top_k = int(data.get('top_k', 5))
    min_similarity = float(data.get('min_similarity', 0.0))

    try:
        results = search_similar_entities(
            query_text=query_text,
            entity_type=entity_type,
            top_k=top_k,
            min_similarity=min_similarity
        )
        return jsonify({
            'success': True,
            'query': query_text,
            'count': len(results),
            'results': results
        }), 200
    except Exception as e:
        logger.error(f"Error en búsqueda semántica: {e}", exc_info=True)
        return jsonify({'error': f'Error en búsqueda semántica: {str(e)}'}), 500
'''

with open(TARGET_PATH, "w", encoding="utf-8") as f:
    f.write(content.strip() + "\n")

print(f"[OK] embeddings.py generado exitosamente en {TARGET_PATH}")
