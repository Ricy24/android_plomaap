import os
import sys
import psycopg2
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
sys.path.insert(0, BACKEND_DIR)
os.chdir(BACKEND_DIR)
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)
pw = os.getenv("POSTGRES_PASSWORD", "")

from app import create_app
from app.database.extensions import db
from app.database.models import AppEvent

app = create_app('development')
client = app.test_client()

print("=" * 65)
print("PRUEBA DEL ENDPOINT GENÉRICO POST /api/events")
print("=" * 65)

# Test 1: Invalid event type
res_inv = client.post('/api/events', json={"event_type": "INVALID_UNKNOWN_EVENT"})
print(f"Test 1 (Tipo inválido): Status={res_inv.status_code}, Body={res_inv.get_json()}")
assert res_inv.status_code == 400
assert res_inv.get_json().get("success") is False

# Test 2: Valid event
res_valid = client.post('/api/events', json={
    "event_type": "SERVICE_VIEWED",
    "entity_type": "service",
    "entity_id": 1,
    "session_id": "test-session-12345",
    "metadata": {"source": "search_results", "test": True}
})
print(f"Test 2 (Tipo válido): Status={res_valid.status_code}, Body={res_valid.get_json()}")
assert res_valid.status_code == 201
event_data = res_valid.get_json().get("event")
assert event_data is not None
test_event_id = event_data.get("id")

# Verify row in database
conn = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pw, dbname="plomapp")
cur = conn.cursor()
cur.execute("SELECT id, event_type, entity_type, entity_id, session_id, metadata FROM public.app_events WHERE id = %s;", (test_event_id,))
row = cur.fetchone()
print(f"Fila insertada en BD: {row}")
assert row[1] == "SERVICE_VIEWED"
assert row[2] == "service"
assert row[3] == 1

# Clean up test event row
cur.execute("DELETE FROM public.app_events WHERE id = %s;", (test_event_id,))
conn.commit()
cur.execute("SELECT COUNT(*) FROM public.app_events;")
cnt = cur.fetchone()[0]
print(f"Conteo en app_events tras limpieza de prueba: {cnt}")
assert cnt == 0
conn.close()

print("[OK] Endpoint POST /api/events probado y limpiado exitosamente.")
