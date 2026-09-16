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
from app.utils.jwt_utils import generate_token

print("=" * 65)
print("PASO 2: PRUEBA DE VERIFICACIÓN EN technicians.py")
print("=" * 65)

app = create_app('development')
client = app.test_client()

with app.app_context():
    # Carlos: technician_id = 2 (assigned to appointment 10)
    token_tech2 = generate_token(2)
    # Roberto: technician_id = 3 (assigned to appointment 1, NOT assigned to appointment 10)
    token_tech3 = generate_token(3)

headers_tech2 = {"Authorization": f"Bearer {token_tech2}"}
headers_tech3 = {"Authorization": f"Bearer {token_tech3}"}

# 1. Prueba con técnico legítimo (Carlos, ID 2) sobre Cita #10 (technician_id = 2)
# Cita #10 original status: 'scheduled'
print("\n[1] Técnico legítimo (user_id=2) actualiza Cita #10 a 'in_progress':")
res_legit = client.patch('/api/technicians/appointments/10', headers=headers_tech2, json={'status': 'in_progress'})
print(f"    Status HTTP: {res_legit.status_code}")
print(f"    Response JSON: {res_legit.get_json()}")
assert res_legit.status_code == 200, f"Esperado 200, obtenido {res_legit.status_code}"
assert res_legit.get_json().get('status') == 'in_progress'

# 2. Prueba con técnico ajeno (Roberto, ID 3) sobre Cita #10 (technician_id = 2)
print("\n[2] Técnico ajeno (user_id=3) intenta actualizar Cita #10:")
res_unauth = client.patch('/api/technicians/appointments/10', headers=headers_tech3, json={'status': 'cancelled'})
print(f"    Status HTTP: {res_unauth.status_code}")
print(f"    Response JSON: {res_unauth.get_json()}")
assert res_unauth.status_code == 403, f"Esperado 403, obtenido {res_unauth.status_code}"
assert res_unauth.get_json().get('error') == 'Unauthorized'

# 3. Revertir Cita #10 a su estado original 'scheduled' y limpiar eventos de prueba
print("\n[3] Revertir Cita #10 a 'scheduled' y limpiar eventos de prueba...")
conn = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pw, dbname="plomapp")
cur = conn.cursor()

cur.execute("UPDATE public.appointments SET status = 'scheduled' WHERE id = 10;")
conn.commit()

cur.execute("SELECT id, status FROM public.appointments WHERE id = 10;")
reverted_row = cur.fetchone()
print(f"    Cita #10 en BD tras reversión: ID={reverted_row[0]}, Status='{reverted_row[1]}'")
assert reverted_row[1] == 'scheduled'

# Clean up any test event logged during the transition if any
cur.execute("DELETE FROM public.app_events WHERE entity_type = 'appointment' AND entity_id = 10;")
conn.commit()

cur.execute("SELECT COUNT(*) FROM public.app_events;")
events_count = cur.fetchone()[0]
print(f"    Total eventos en app_events tras limpieza: {events_count}")
assert events_count == 0

conn.close()

print("\n" + "=" * 65)
print("PRUEBA DE VERIFICACIÓN PASÓ EXITOSAMENTE (100%)")
print("=" * 65)
