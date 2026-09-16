import os
import sys
import psycopg2
import pymysql
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
sys.path.insert(0, BACKEND_DIR)
os.chdir(BACKEND_DIR)
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)
pw = os.getenv("POSTGRES_PASSWORD", "")

from app import create_app

print("=" * 65)
print("PASO 5: PRUEBAS FUNCIONALES DE REGRESIÓN")
print("=" * 65)

app = create_app('development')
client = app.test_client()

# 1. Login
res_login = client.post('/api/auth/login', json={'email': 'carlos@plomaap.com', 'password': '123456'})
print(f"1. Login: Status={res_login.status_code}")
assert res_login.status_code == 200
token = res_login.get_json().get('token')
headers = {'Authorization': f'Bearer {token}'}

# 2. Listado de servicios (deben verse los 4 reales)
res_services = client.get('/api/services')
services = res_services.get_json()
print(f"2. Servicios: Status={res_services.status_code}, Total devueltos={len(services)}")
assert res_services.status_code == 200
assert len(services) == 4

# 3. Listado de citas
res_apts = client.get('/api/appointments', headers=headers)
apts = res_apts.get_json().get('appointments', [])
print(f"3. Citas: Status={res_apts.status_code}, Citas del usuario={len(apts)}")
assert res_apts.status_code == 200

# 4. Listado de direcciones guardadas
res_addrs = client.get('/api/user/addresses', headers=headers)
print(f"4. Direcciones: Status={res_addrs.status_code}, Body={res_addrs.get_json()}")
assert res_addrs.status_code == 200

# 5. Listado de favoritos
res_favs = client.get('/api/user/favorites', headers=headers)
favs = res_favs.get_json().get('favorites', [])
print(f"5. Favoritos: Status={res_favs.status_code}, Favoritos={len(favs)}")
assert res_favs.status_code == 200

# 6. Conteo de tablas en Docker 5434
print("\n[*] Verificando conteos de tablas en Docker (5434):")
conn = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pw, dbname="plomapp")
cur = conn.cursor()
expected = {
    "users": 14,
    "services": 4,
    "appointments": 10,
    "user_addresses": 6,
    "user_favorites": 2,
    "technician_profiles": 2,
    "devices": 0,
    "webauthn_credentials": 0,
    "room_type_catalog": 7,
    "asset_type_catalog": 8,
    "homes": 0,
    "home_members": 0,
    "saved_locations": 0,
    "home_rooms": 0,
    "home_assets": 0,
    "app_events": 0,
    "recommendation_events": 0
}
all_ok = True
for tbl, exp in expected.items():
    cur.execute(f"SELECT COUNT(*) FROM public.{tbl};")
    c = cur.fetchone()[0]
    ok = (c == exp)
    if not ok: all_ok = False
    print(f"    - public.{tbl:<22}: {c:2d} (esperado: {exp:2d}) -> {'OK' if ok else 'ERROR'}")
assert all_ok
conn.close()

# 7. Native PG 5432
conn_nat = psycopg2.connect(host="localhost", port=5432, user="postgres", password=pw, dbname="plomapp")
cur_nat = conn_nat.cursor()
cur_nat.execute("SELECT COUNT(*) FROM public.users;")
nat_users = cur_nat.fetchone()[0]
cur_nat.execute("SELECT COUNT(*) FROM public.user_addresses;")
nat_addrs = cur_nat.fetchone()[0]
cur_nat.execute("SELECT COUNT(*) FROM public.appointments;")
nat_apts = cur_nat.fetchone()[0]
print(f"\n[*] PostgreSQL Nativo (5432): users={nat_users}, addresses={nat_addrs}, appointments={nat_apts} (INTACTO)")
assert nat_users == 14 and nat_addrs == 6 and nat_apts == 10
conn_nat.close()

# 8. MySQL 3306
conn_my = pymysql.connect(
    host=os.getenv("MYSQL_HOST", "localhost"),
    user=os.getenv("MYSQL_USER", "root"),
    password=os.getenv("MYSQL_PASSWORD", ""),
    port=int(os.getenv("MYSQL_PORT", 3306)),
    database=os.getenv("MYSQL_DB", "plomapp")
)
cur_my = conn_my.cursor()
cur_my.execute("SHOW TABLES;")
my_tables = cur_my.fetchall()
total_my = 0
for t in my_tables:
    cur_my.execute(f"SELECT COUNT(*) FROM `{t[0]}`;")
    total_my += cur_my.fetchone()[0]
print(f"[*] MySQL (3306): {len(my_tables)} tablas, {total_my} registros (INTACTO)")
assert len(my_tables) == 19 and total_my == 139
conn_my.close()

print("\n" + "=" * 65)
print("TODAS LAS PRUEBAS DE REGRESIÓN PASARON AL 100%")
print("=" * 65)
