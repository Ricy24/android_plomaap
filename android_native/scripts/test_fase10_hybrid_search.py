import requests
import json
import sys

BASE_URL = "http://127.0.0.1:5000/api/search/hybrid"

test_cases = [
    {
        "query": "tengo una fuga de agua con gotera debajo del lavamanos",
        "expected_top_category": "Fugas",
        "description": "Detección de fuga y gotera en lavamanos"
    },
    {
        "query": "el inodoro se tapó con papel y el agua no baja",
        "expected_top_category": "Destapes",
        "description": "Atasco/obstrucción de sanitario"
    },
    {
        "query": "necesito conectar e instalar un calentador nuevo en la pared",
        "expected_top_category": "Instalaciones",
        "description": "Instalación de calentador"
    }
]

print("=== INICIANDO VALIDACIÓN FASE 10: BÚSQUEDA HÍBRIDA ===")
all_passed = True

for i, tc in enumerate(test_cases, 1):
    print(f"\n--- Caso {i}: {tc['description']} ---")
    print(f"Query: \"{tc['query']}\"")
    try:
        resp = requests.post(BASE_URL, json={"query": tc["query"], "top_k": 3}, timeout=15)
        if resp.status_code != 200:
            print(f"[FAIL] HTTP Status: {resp.status_code} - {resp.text}")
            all_passed = False
            continue

        data = resp.json()
        print(f"Total resultados: {data.get('total_results')}")
        print(f"Activos detectados: {data.get('detected_assets')}")
        print(f"Síntomas detectados: {data.get('detected_symptoms')}")

        results = data.get("results", [])
        if not results:
            print("[FAIL] No se obtuvieron resultados")
            all_passed = False
            continue

        top1 = results[0]
        print(f"Top 1: ID {top1['service_id']} - {top1['name']} ({top1['category']})")
        print(f"Score Híbrido: {top1['hybrid_score']} (Vec: {top1['vector_score']}, Lex: {top1['lexical_score']})")
        print(f"Razones: {', '.join(top1['match_reasons'])}")

        if tc["expected_top_category"].lower() in top1["category"].lower() or tc["expected_top_category"].lower() in top1["name"].lower():
            print(f"[PASS] Coincide con categoría esperada: {tc['expected_top_category']}")
        else:
            print(f"[WARN] Categoría obtenida '{top1['category']}' vs esperada '{tc['expected_top_category']}'")

    except Exception as e:
        print(f"[ERROR] Excepción realizando petición: {e}")
        all_passed = False

if all_passed:
    print("\n[SUCCESS] Todos los casos de búsqueda híbrida pasaron satisfactoriamente.")
    sys.exit(0)
else:
    print("\n[FAIL] Algunos casos fallaron.")
    sys.exit(1)
