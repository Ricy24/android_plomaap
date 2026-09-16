import os
import sys
import psycopg2
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
sys.path.insert(0, BACKEND_DIR)
os.chdir(BACKEND_DIR)
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)

from app import create_app
from app.utils.jwt_utils import generate_token

app = create_app('development')
client = app.test_client()

print("=" * 65)
print("MATRIZ DE PRUEBAS DE AUTORIZACIÓN: ANTES VS DESPUÉS")
print("=" * 65)

# Cita 1: user_id = 1 (customer Andrés), technician_id = 3 (technician Roberto), status = completed
# Cita 10: user_id = 14 (customer prueba), technician_id = 2 (technician Carlos), status = scheduled

with app.app_context():
    token_user1 = generate_token(1)    # Customer Andrés
    token_user14 = generate_token(14)  # Customer prueba
    token_tech2 = generate_token(2)    # Technician Carlos (assigned to appointment 10)
    token_tech3 = generate_token(3)    # Technician Roberto (assigned to appointment 1)

headers_user1 = {"Authorization": f"Bearer {token_user1}"}
headers_user14 = {"Authorization": f"Bearer {token_user14}"}
headers_tech2 = {"Authorization": f"Bearer {token_tech2}"}
headers_tech3 = {"Authorization": f"Bearer {token_tech3}"}

# Escenario 1: Dueño legítimo accede a su propia cita (GET /api/appointments/1 con user 1)
print("\n--- ESCENARIO 1: Dueño legítimo accede a su propia cita ---")
res_e1 = client.get('/api/appointments/1', headers=headers_user1)
print(f"Resultado ACTUAL con código en appointments.py: Status={res_e1.status_code}")
# Evaluación código ANTERIOR:
# In old code: appointment.user_id = 1, user_id = '1'.
# condition: if user.role == 'customer' and appointment.user_id != user_id:
# 'customer' == 'customer' AND (1 != '1') -> True AND True -> True -> return 403
old_e1 = 403
print(f"Resultado con código ANTERIOR: {old_e1} (1 != '1' evaluaba a True)")

# Escenario 2: Usuario intenta acceder a cita ajena (GET /api/appointments/10 con user 1)
# Appointment 10 belongs to user 14
print("\n--- ESCENARIO 2: Usuario intenta acceder a cita ajena ---")
res_e2 = client.get('/api/appointments/10', headers=headers_user1)
print(f"Resultado ACTUAL con código en appointments.py: Status={res_e2.status_code}")
# In old code: appointment.user_id = 10, user_id = '1'. 10 != '1' is True -> 403
old_e2 = 403
print(f"Resultado con código ANTERIOR: {old_e2}")

# Escenario 3: Técnico asignado actualiza estado de su cita asignada
# Appointment 10: assigned to tech 2 (Carlos). Status is 'scheduled'.
# Let's test PATCH /api/technicians/appointments/10 with tech 2
print("\n--- ESCENARIO 3: Técnico asignado actualiza estado de su cita asignada ---")
# Call technicians endpoint:
res_e3_tech_route = client.patch('/api/technicians/appointments/10', headers=headers_tech2, json={'status': 'in_progress'})
print(f"Llamada a /api/technicians/appointments/10 (código SIN corregir en technicians.py): Status={res_e3_tech_route.status_code}, Body={res_e3_tech_route.get_json()}")

# Also check /api/appointments/10 if called by technician (it is customer/admin only for PATCH, but tech can GET)
res_e3_get = client.get('/api/appointments/10', headers=headers_tech2)
print(f"Llamada a GET /api/appointments/10 por el técnico asignado (código CORREGIDO en appointments.py): Status={res_e3_get.status_code}")

# Escenario 4: Usuario intenta actualizar cita ajena (PATCH /api/appointments/10 con user 1)
# Appointment 10 belongs to user 14
print("\n--- ESCENARIO 4: Usuario intenta actualizar cita ajena ---")
res_e4 = client.patch('/api/appointments/10', headers=headers_user1, json={'status': 'cancelled'})
print(f"Resultado ACTUAL con código en appointments.py: Status={res_e4.status_code}, Body={res_e4.get_json()}")
old_e4 = 403
print(f"Resultado con código ANTERIOR: {old_e4}")
