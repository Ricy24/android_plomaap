
import os
import sys

TARGET_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\services\embedding_service.py"

content = '''"""
Vector Embedding and Semantic Search Service for PlomApp
FASE 9: Embedding Provider (Secciones 16, 17 y 48 del Plan Maestro)
"""
import logging
from datetime import datetime
from sqlalchemy import text
from app.database.extensions import db
from app.database.models import Service, AssetTypeCatalog
from app.services.embedding_provider import get_embedding_provider, BaseEmbeddingProvider

logger = logging.getLogger(__name__)


def _vector_to_sql_str(vec: list) -> str:
    """Convierte lista de floats en formato string para pgvector [0.1,0.2,...]"""
    return "[" + ",".join(str(round(float(x), 6)) for x in vec) + "]"


def store_entity_embedding(entity_type: str, entity_id: int, content_text: str, provider: BaseEmbeddingProvider = None, precomputed_vector: list = None, commit=True):
    """
    Guarda el embedding para una entidad y realiza upsert en public.entity_embeddings.
    Si precomputed_vector es provisto, evita llamar a la API externa.
    """
    prov = provider or get_embedding_provider()
    content_clean = str(content_text).strip()
    if not content_clean:
        raise ValueError("content_text no puede estar vacío")
        
    vec = precomputed_vector if precomputed_vector is not None else prov.embed_text(content_clean)
    vec_sql = _vector_to_sql_str(vec)
    now = datetime.utcnow()

    # Usar CAST(:embedding AS vector) para evitar colisión de dos puntos (::) con bind params de SQLAlchemy
    sql = text("""
        INSERT INTO public.entity_embeddings (
            entity_type, entity_id, content_text, embedding, model_name, dimension, created_at, updated_at
        ) VALUES (
            :entity_type, :entity_id, :content_text, CAST(:embedding AS vector), :model_name, :dimension, :created_at, :updated_at
        )
        ON CONFLICT ON CONSTRAINT uq_entity_embedding DO UPDATE SET
            content_text = EXCLUDED.content_text,
            embedding = EXCLUDED.embedding,
            dimension = EXCLUDED.dimension,
            updated_at = EXCLUDED.updated_at;
    """)

    params = {
        'entity_type': str(entity_type),
        'entity_id': int(entity_id),
        'content_text': content_clean,
        'embedding': vec_sql,
        'model_name': prov.model_name,
        'dimension': prov.dimension,
        'created_at': now,
        'updated_at': now
    }

    try:
        db.session.execute(sql, params)
        if commit:
            db.session.commit()
        return True
    except Exception as e:
        if commit:
            db.session.rollback()
        logger.error(f"Error guardando embedding {entity_type}#{entity_id}: {e}", exc_info=True)
        raise e


def search_similar_entities(query_text: str, entity_type: str = None, top_k: int = 5, min_similarity: float = 0.0, provider: BaseEmbeddingProvider = None) -> list:
    """
    Realiza búsqueda vectorial semántica por similitud coseno en public.entity_embeddings.
    Retorna lista ordenada de coincidencias con entidad resuelta, similarity (0 a 1) y metadatos.
    """
    query_clean = str(query_text).strip()
    if not query_clean:
        return []

    prov = provider or get_embedding_provider()
    query_vec = prov.embed_text(query_clean)
    query_vec_sql = _vector_to_sql_str(query_vec)

    sql = text("""
        SELECT id, entity_type, entity_id, content_text, model_name, dimension,
               (1 - (embedding <=> CAST(:vec AS vector))) AS similarity
        FROM public.entity_embeddings
        WHERE (:entity_type IS NULL OR entity_type = :entity_type)
          AND model_name = :model_name
          AND (1 - (embedding <=> CAST(:vec AS vector))) >= :min_similarity
        ORDER BY embedding <=> CAST(:vec AS vector) ASC
        LIMIT :top_k;
    """)

    params = {
        'vec': query_vec_sql,
        'entity_type': entity_type if entity_type else None,
        'model_name': prov.model_name,
        'min_similarity': float(min_similarity),
        'top_k': int(top_k)
    }

    rows = db.session.execute(sql, params).fetchall()
    results = []

    # Enriquecer con datos de entidad si es service o asset_type
    service_ids = [r[2] for r in rows if r[1] == 'service']
    asset_ids = [r[2] for r in rows if r[1] == 'asset_type']

    services_map = {}
    if service_ids:
        for s in Service.query.filter(Service.id.in_(service_ids)).all():
            services_map[s.id] = s.to_dict()

    assets_map = {}
    if asset_ids:
        for a in AssetTypeCatalog.query.filter(AssetTypeCatalog.id.in_(asset_ids)).all():
            assets_map[a.id] = a.to_dict()

    for r in rows:
        r_id, r_type, r_entity_id, r_content, r_model, r_dim, r_sim = r
        sim_val = float(r_sim) if r_sim is not None else 0.0
        
        entity_obj = None
        if r_type == 'service':
            entity_obj = services_map.get(r_entity_id)
        elif r_type == 'asset_type':
            entity_obj = assets_map.get(r_entity_id)

        results.append({
            'embedding_id': r_id,
            'entity_type': r_type,
            'entity_id': r_entity_id,
            'similarity': round(sim_val, 4),
            'content_text': r_content,
            'model_name': r_model,
            'entity': entity_obj
        })

    return results


def sync_catalog_embeddings(commit=True, provider: BaseEmbeddingProvider = None) -> dict:
    """
    Sincroniza embeddings para los 4 servicios reales y los 8 tipos de activo del catálogo.
    Utiliza embed_batch() para generar todos los vectores en una sola llamada de red.
    """
    prov = provider or get_embedding_provider()
    
    # 1. Recopilar entidades a sincronizar
    items_to_sync = []
    
    services = Service.query.all()
    for s in services:
        text_repr = f"Servicio: {s.name}. Categoría: {s.category}. Descripción: {s.description}."
        items_to_sync.append({
            'type': 'service',
            'id': s.id,
            'text': text_repr
        })

    assets = AssetTypeCatalog.query.filter_by(is_active=True).all()
    for a in assets:
        text_repr = f"Activo del hogar: {a.name}. Categoría técnica: {a.category}. Código: {a.code}."
        items_to_sync.append({
            'type': 'asset_type',
            'id': a.id,
            'text': text_repr
        })

    if not items_to_sync:
        return {'services_synced': 0, 'assets_synced': 0, 'total_synced': 0}

    # 2. Generar todos los embeddings en BATCH (1 sola llamada API para evitar rate limit de 3 RPM)
    all_texts = [item['text'] for item in items_to_sync]
    all_vectors = prov.embed_batch(all_texts)

    # 3. Guardar en base de datos
    synced_services = 0
    synced_assets = 0
    for item, vec in zip(items_to_sync, all_vectors):
        store_entity_embedding(
            item['type'],
            item['id'],
            item['text'],
            provider=prov,
            precomputed_vector=vec,
            commit=False
        )
        if item['type'] == 'service':
            synced_services += 1
        elif item['type'] == 'asset_type':
            synced_assets += 1

    if commit:
        db.session.commit()

    return {
        'provider': prov.model_name,
        'dimension': prov.dimension,
        'services_synced': synced_services,
        'assets_synced': synced_assets,
        'total_synced': synced_services + synced_assets
    }
'''

with open(TARGET_PATH, "w", encoding="utf-8") as f:
    f.write(content.strip() + "\n")

print(f"[OK] embedding_service.py actualizado con CAST(:vec AS vector)")
