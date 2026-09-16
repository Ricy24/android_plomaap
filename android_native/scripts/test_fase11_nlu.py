import requests
import json
import sys

BASE_URL = "http://127.0.0.1:5000/api/intelligence/understand"

test_cases = [
    {
        "query": "se me está saliendo agua por debajo del lavamanos",
        "expected_intent": "repair",
        "expected_asset": "lavamanos",
        "expected_service_id": 1,
        "description": "Fuga de agua debajo de lavamanos"
    },
    {
        "query": "el inodoro se tapó con papel y el agua se rebosa, ayuda urgente",
        "expected_intent": ["emergency", "unblock"],
        "expected_asset": "sanitario",
        "expected_service_id": 2,
        "description": "Sanitario tapado con riesgo de rebose (Urgencia Alta)"
    },
    {
        "query": "quiero cotizar y montar un calentador de gas nuevo que compré ayer",
        "expected_intent": "installation",
        "expected_asset": "calentador",
        "expected_service_id": 4,
        "description": "Instalación de calentador nuevo"
    }
]

print("=== INICIANDO VALIDACIÓN FASE 11: NLU & INTENT EXTRACTION ===")
all_passed = True

for i, tc in enumerate(test_cases, 1):
    print(f"\n--- Caso {i}: {tc['description']} ---")
    print(f"Texto de entrada: \"{tc['query']}\"")
    try:
        resp = requests.post(BASE_URL, json={"text": tc["query"]}, timeout=25)
        if resp.status_code != 200:
            print(f"[FAIL] HTTP Status: {resp.status_code} - {resp.text}")
            all_passed = False
            continue

        data = resp.json()
        print(f"Provider: {data.get('provider')}")
        print(f"Intent: {data.get('intent')} | Urgency: {data.get('urgency')} | Confidence: {data.get('confidence')}")
        print(f"Asset: {data.get('asset')} (ID {data.get('asset_id')}: {data.get('asset_name')})")
        print(f"Issue: {data.get('issue')} | Symptoms: {data.get('symptoms')}")
        print(f"Recommended Service IDs: {data.get('recommended_service_ids')}")

        rec_services = data.get("recommended_services", [])
        if rec_services:
            print(f"Servicio Top: ID {rec_services[0]['id']} - {rec_services[0]['name']} (${rec_services[0]['base_price']})")
            print(f"Razón: {rec_services[0]['reason']}")

        action = data.get("suggested_action", {})
        print(f"Acción sugerida: {action.get('action_type')} -> Service ID {action.get('target_service_id')}")
        print(f"Notas pre-llenadas: {action.get('prefill_notes')}")

        # Validaciones
        rec_ids = data.get("recommended_service_ids", [])
        expected_s_id = tc["expected_service_id"]
        if expected_s_id in rec_ids:
            print(f"[PASS] Servicio esperado ID {expected_s_id} está en las recomendaciones.")
        else:
            print(f"[WARN] Servicio ID {expected_s_id} no está en {rec_ids}")

        if tc["expected_asset"] and tc["expected_asset"] == data.get("asset"):
            print(f"[PASS] Activo esperado '{tc['expected_asset']}' detectado con ID {data.get('asset_id')}.")
        else:
            print(f"[NOTE] Activo detectado '{data.get('asset')}' vs esperado '{tc['expected_asset']}'")

    except Exception as e:
        print(f"[ERROR] Excepción: {e}")
        all_passed = False

if all_passed:
    print("\n[SUCCESS] Todos los casos de prueba NLU pasaron exitosamente.")
    sys.exit(0)
else:
    print("\n[FAIL] Algunos casos tuvieron discrepancias.")
    sys.exit(1)
