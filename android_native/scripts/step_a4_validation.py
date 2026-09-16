import os
import sys
import json
from dotenv import load_dotenv

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
sys.path.insert(0, BACKEND_DIR)
os.chdir(BACKEND_DIR)
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)

from app import create_app
from app.database.extensions import db
from app.database.models import User, Service, Appointment, UserAddress, UserFavorite

print("=" * 65)
print("PARTE A - PASO A4: VALIDACIÓN FUNCIONAL CONTRA FLASK REAL (PUERTO 5434)")
print("=" * 65)

app = create_app('development')
client = app.test_client()

# 1. Verify active connection is 5434
with app.app_context():
    url_str = str(db.engine.url)
    print(f"Engine URL en Flask: {db.engine.name}")
    assert ":5434/" in url_str, f"ERROR: Flask no está conectado al puerto 5434: {url_str}"
    print("[OK] Confirmado: Flask está conectado a PostgreSQL en el contenedor (puerto 5434).")

# Health check
res_health = client.get("/api/health")
print("\n[0] /api/health -> Status:", res_health.status_code, "Body:", res_health.get_json())
assert res_health.status_code == 200
assert res_health.get_json().get("database") == "connected"

# Test 1: Login de un usuario existente
print("\n[1] Prueba Funcional 1: Login con usuario existente (carlos@plomaap.com)...")
res_login = client.post("/api/auth/login", json={"email": "carlos@plomaap.com", "password": "123456"})
print("    Status login:", res_login.status_code)
login_body = res_login.get_json()
token = login_body.get("access_token") or login_body.get("token") or login_body.get("data", {}).get("token")
print("    Token recibido?", bool(token))
assert res_login.status_code == 200
assert token is not None, "Error: No se obtuvo token JWT"
headers_tech = {"Authorization": f"Bearer {token}"}

# Test 2: Listado de servicios (deben verse los 4 reales)
print("\n[2] Prueba Funcional 2: Listado de servicios (/api/services)...")
res_services = client.get("/api/services")
print("    Status servicios:", res_services.status_code)
services_data = res_services.get_json()
services_list = services_data if isinstance(services_data, list) else services_data.get("data", services_data.get("services", []))
print(f"    Servicios devueltos: {len(services_list)}")
for s in services_list:
    print(f"      - ID {s.get('id')}: {s.get('name')} (${s.get('base_price')})")
assert res_services.status_code == 200
assert len(services_list) == 4, f"Esperados 4 servicios, obtenidos {len(services_list)}"

# Login as customer user 1 (andres@gmail.com / password from bcrypt or admin@plomaap.com)
# Let's check admin@plomaap.com / Admin123!
res_admin = client.post("/api/auth/login", json={"email": "admin@plomaap.com", "password": "Admin123!"})
if res_admin.status_code == 200:
    admin_token = res_admin.get_json().get("access_token") or res_admin.get_json().get("token") or res_admin.get_json().get("data", {}).get("token")
    headers_admin = {"Authorization": f"Bearer {admin_token}"}
else:
    headers_admin = headers_tech

# Test 3: Listado de citas de un usuario con citas existentes
print("\n[3] Prueba Funcional 3: Listado de citas (/api/appointments)...")
res_apts = client.get("/api/appointments", headers=headers_tech)
print("    Status citas (carlos):", res_apts.status_code)
apts_data = res_apts.get_json()
apts_list = apts_data if isinstance(apts_data, list) else apts_data.get("data", apts_data.get("appointments", []))
print(f"    Citas encontradas para el técnico: {len(apts_list)}")
assert res_apts.status_code == 200
# Also verify appointments endpoint for admin or total in db
with app.app_context():
    total_apts = Appointment.query.count()
    print(f"    Total de citas en la base de datos: {total_apts} (esperado: 10)")
    assert total_apts == 10

# Test 4: Listado de direcciones guardadas
print("\n[4] Prueba Funcional 4: Listado de direcciones guardadas (/api/user/addresses)...")
# User 1 (Casa, Carrera 7) has saved addresses
with app.app_context():
    total_addresses = UserAddress.query.count()
    print(f"    Total de direcciones en la base de datos: {total_addresses} (esperado: 6)")
    assert total_addresses == 6

# Test with user who has addresses or test address query
res_addr = client.get("/api/user/addresses", headers=headers_tech)
print("    Status direcciones endpoint:", res_addr.status_code)
assert res_addr.status_code in [200, 404] # 200 if carlos has addresses, empty list

# Test 5: Listado de favoritos
print("\n[5] Prueba Funcional 5: Listado de favoritos (/api/user/favorites)...")
with app.app_context():
    total_favs = UserFavorite.query.count()
    print(f"    Total de favoritos en la base de datos: {total_favs} (esperado: 2)")
    assert total_favs == 2

res_fav = client.get("/api/user/favorites", headers=headers_tech)
print("    Status favoritos endpoint:", res_fav.status_code)
assert res_fav.status_code == 200

print("\n" + "=" * 65)
print("PARTE A COMPLETADA CON ÉXITO: CUTOVER CONFIRMADO Y 100% OPERATIVO")
print("=" * 65)
