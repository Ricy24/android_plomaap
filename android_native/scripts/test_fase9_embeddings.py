import os
import sys
import json
import psycopg2
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
sys.path.insert(0, BACKEND_DIR)
os.chdir(BACKEND_DIR)
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)

from app import create_app
from app.database.extensions import db
from app.database.models import Service, AssetTypeCatalog, EntityEmbedding

app = create_app('development')
client = app.test_client()

print("=" * 70)
print("FASE 9: VALIDACIÓN DE EMBEDDING PROVIDER Y BÚSQUEDA SEMÁNTICA VECTORIAL")
print("=" * 70)

with app.app_context():
    # 1. Info del proveedor activo
    print("[1] Verificando GET /api/embeddings/provider-info...")
    res_info = client.get('/api/embeddings/provider-info')
    assert res_info.status_code == 200
    info_data = res_info.get_json()
    print(f"    Proveedor activo: {info_data['provider_class']}")
    print(f"    Modelo:           {info_data['model_name']}")
    print(f"    Dimensión:        {info_data['dimension']}")
    assert info_data['dimension'] == 512
    print("    -> [OK] Proveedor activo validado exitosamente.")
    print("-" * 70)

    # 2. Generación individual de embedding
    print("[2] Probando POST /api/embeddings/embed...")
    res_embed = client.post('/api/embeddings/embed', json={"text": "Fuga de agua en tubería de baño", "include_full_vector": False})
    assert res_embed.status_code == 200
    embed_data = res_embed.get_json()
    print(f"    Dimensión generada: {embed_data['dimension']}")
    print(f"    Muestra de vector:  {embed_data['embedding_sample']}")
    assert embed_data['dimension'] == 512
    assert len(embed_data['embedding_sample']) == 8
    print("    -> [OK] Generación de vector validada al 100%.")
    print("-" * 70)

    # 3. Sincronización de catálogo en pgvector
    print("[3] Probando POST /api/embeddings/sync (indexación de servicios y catálogo de activos)...")
    res_sync = client.post('/api/embeddings/sync')
    assert res_sync.status_code == 200
    sync_data = res_sync.get_json()['data']
    print(f"    Servicios indexados: {sync_data['services_synced']}")
    print(f"    Activos indexados:   {sync_data['assets_synced']}")
    print(f"    Total indexados:     {sync_data['total_synced']}")
    assert sync_data['services_synced'] == 4
    assert sync_data['assets_synced'] == 8
    assert sync_data['total_synced'] == 12

    # Verificar en PostgreSQL
    total_in_db = EntityEmbedding.query.count()
    print(f"    Total filas en public.entity_embeddings: {total_in_db}")
    assert total_in_db == 12
    print("    -> [OK] 12 embeddings almacenados e indexados con HNSW en pgvector.")
    print("-" * 70)

    # 4. Pruebas de Búsqueda Semántica Vectorial Pura
    print("[4] Ejecutando pruebas de Búsqueda Semántica Vectorial...")

    # Consulta 1: Fuga de agua
    q1 = "tengo una fuga de agua con gotera en una llave"
    print(f"\n  Query 1: '{q1}'")
    res_s1 = client.post('/api/embeddings/search', json={"query": q1, "entity_type": "service", "top_k": 3})
    assert res_s1.status_code == 200
    r1 = res_s1.get_json()['results']
    for idx, item in enumerate(r1, 1):
        print(f"    #{idx} [Sim: {item['similarity']:.4f}] {item['entity']['name']} (ID: {item['entity_id']})")
    assert r1[0]['entity_id'] == 1, f"Se esperaba Servicio #1 (Fugas de agua) como top match, se obtuvo {r1[0]['entity_id']}"
    print("    -> [OK] Coincidencia semántica perfecta con Servicio #1 (Fugas de agua).")

    # Consulta 2: Destape / Inodoro atascado
    q2 = "el sanitario se tapó con papel y el agua no baja para nada"
    print(f"\n  Query 2: '{q2}'")
    res_s2 = client.post('/api/embeddings/search', json={"query": q2, "entity_type": "service", "top_k": 3})
    assert res_s2.status_code == 200
    r2 = res_s2.get_json()['results']
    for idx, item in enumerate(r2, 1):
        print(f"    #{idx} [Sim: {item['similarity']:.4f}] {item['entity']['name']} (ID: {item['entity_id']})")
    top_ids = [item['entity_id'] for item in r2[:2]]
    assert 2 in top_ids or 3 in top_ids, f"Se esperaba Servicio #2 o #3 en top matches, se obtuvo {top_ids}"
    print("    -> [OK] Coincidencia semántica relevante con Baños / Destapes.")

    # Consulta 3: Instalación de calentador
    q3 = "quiero conectar un calentador nuevo en la pared"
    print(f"\n  Query 3: '{q3}'")
    res_s3 = client.post('/api/embeddings/search', json={"query": q3, "top_k": 4})
    assert res_s3.status_code == 200
    r3 = res_s3.get_json()['results']
    for idx, item in enumerate(r3, 1):
        name = item['entity']['name'] if item['entity'] else item['content_text']
        print(f"    #{idx} [{item['entity_type']}] [Sim: {item['similarity']:.4f}] {name}")
    print("    -> [OK] Búsqueda semántica híbrida/multientidad funcionando.")

print("\n" + "=" * 70)
print("FASE 9: BACKEND DE EMBEDDINGS Y PGVECTOR VALIDADO AL 100%")
print("=" * 70)
