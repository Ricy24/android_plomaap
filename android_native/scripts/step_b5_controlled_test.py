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
pw = os.getenv("POSTGRES_PASSWORD", "")

from app import create_app
from app.database.extensions import db
from app.database.models import Appointment, AppEvent
from app.utils.jwt_utils import generate_token

print("=" * 65)
print("PASO B5: PRUEBA CONTROLADA DE EVENT TRACKING")
print("=" * 65)

app = create_app('development')
client = app.test_client()

# Customer token (User ID 1 - Andrés)
with app.app_context():
    token_user1 = generate_token(1)
headers_customer = {"Authorization": f"Bearer {token_user1}"}

# 1. Verify appointments baseline is 10
conn = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pw, dbname="plomapp")
cur = conn.cursor()
cur.execute("SELECT COUNT(*) FROM public.appointments;")
apt_before = cur.fetchone()[0]
print(f"[*] Citas antes de la prueba: {apt_before} (esperado: 10)")
assert apt_before == 10

# 2. Create test appointment
payload_apt = {
    "service_id": 1,
    "date": "2026-12-31",
    "time": "10:00",
    "address": "Calle 123 # 45-67 (Prueba Controlada)",
    "address_reference": "Apt 999",
    "notes": "Prueba de instrumentación de eventos FASE 6"
}

res_create = client.post('/api/appointments', headers=headers_customer, json=payload_apt)
print(f"\n[1] Creación de cita: Status={res_create.status_code}")
assert res_create.status_code == 201, f"Error creando cita: {res_create.get_json()}"
create_json = res_create.get_json()
apt_id = create_json.get("appointment", {}).get("id")
print(f"    ID de cita generada: {apt_id}")

# Verify BOOKING_CREATED in app_events
cur.execute("""
SELECT id, event_type, entity_type, entity_id, user_id, metadata, created_at
FROM public.app_events
WHERE entity_type = 'appointment' AND entity_id = %s AND event_type = 'BOOKING_CREATED';
""", (apt_id,))
event_created = cur.fetchone()
print(f"[OK] Evento detectado en BD (BOOKING_CREATED):")
print(f"     ID: {event_created[0]}")
print(f"     Type: {event_created[1]}")
print(f"     Entity: {event_created[2]} #{event_created[3]}")
print(f"     User ID: {event_created[4]}")
print(f"     Metadata: {json.dumps(event_created[5], ensure_ascii=False)}")
print(f"     Created At: {event_created[6]}")
assert event_created is not None
assert event_created[1] == 'BOOKING_CREATED'
assert event_created[4] == 1

# 3. Cancel test appointment
res_cancel = client.patch(f'/api/appointments/{apt_id}', headers=headers_customer, json={"status": "cancelled", "reason": "Test cancellation"})
print(f"\n[2] Cancelación de cita: Status={res_cancel.status_code}")
assert res_cancel.status_code == 200

# Verify BOOKING_CANCELLED in app_events
cur.execute("""
SELECT id, event_type, entity_type, entity_id, user_id, metadata, created_at
FROM public.app_events
WHERE entity_type = 'appointment' AND entity_id = %s AND event_type = 'BOOKING_CANCELLED';
""", (apt_id,))
event_cancelled = cur.fetchone()
print(f"[OK] Evento detectado en BD (BOOKING_CANCELLED):")
print(f"     ID: {event_cancelled[0]}")
print(f"     Type: {event_cancelled[1]}")
print(f"     Entity: {event_cancelled[2]} #{event_cancelled[3]}")
print(f"     User ID: {event_cancelled[4]}")
print(f"     Metadata: {json.dumps(event_cancelled[5], ensure_ascii=False)}")
print(f"     Created At: {event_cancelled[6]}")
assert event_cancelled is not None
assert event_cancelled[1] == 'BOOKING_CANCELLED'
assert event_cancelled[4] == 1

# 4. Cleanup: Remove the test appointment and reset appointments sequence to 10
print("\n[3] Limpieza de la cita de prueba...")
cur.execute("DELETE FROM public.appointments WHERE id = %s;", (apt_id,))
conn.commit()

# Reset appointments sequence to 10 (or max id)
cur.execute("SELECT MAX(id) FROM public.appointments;")
max_id = cur.fetchone()[0]
cur.execute(f"SELECT setval('public.appointments_id_seq', {max_id}, true);")
conn.commit()

cur.execute("SELECT COUNT(*) FROM public.appointments;")
apt_after = cur.fetchone()[0]
print(f"    Total citas tras limpieza: {apt_after} (esperado: 10)")
assert apt_after == 10

# Clean up the test events from app_events as well to leave table completely clean
cur.execute("DELETE FROM public.app_events WHERE entity_type = 'appointment' AND entity_id = %s;", (apt_id,))
conn.commit()
cur.execute("SELECT COUNT(*) FROM public.app_events;")
events_count = cur.fetchone()[0]
print(f"    Total eventos en app_events tras limpieza de prueba: {events_count}")
assert events_count == 0

conn.close()

print("\n" + "=" * 65)
print("PRUEBA CONTROLADA PASÓ AL 100% CON ÉXITO")
print("=" * 65)
