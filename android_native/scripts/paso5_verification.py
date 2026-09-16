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
from app.database.extensions import db
from app.database.models import User, UserAddress

print("=" * 65)
print("PASO 5: VERIFICACIÓN DEL FIX DE GET /api/user/addresses")
print("=" * 65)

app = create_app('development')
client = app.test_client()

# Helper to query DB count directly
def get_db_address_count():
    conn = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pw, dbname="plomapp")
    cur = conn.cursor()
    cur.execute("SELECT COUNT(*) FROM public.user_addresses;")
    cnt = cur.fetchone()[0]
    conn.close()
    return cnt

initial_count = get_db_address_count()
print(f"[*] Conteo inicial en BD (Docker 5434): {initial_count}")
assert initial_count == 6, f"ERROR: Conteo inicial esperado 6, obtenido {initial_count}"

# Login with Carlos (carlos@plomaap.com, user_id=2) who has user.address != None but 0 addresses in user_addresses
res_login = client.post("/api/auth/login", json={"email": "carlos@plomaap.com", "password": "123456"})
assert res_login.status_code == 200
token_carlos = res_login.get_json().get("token") or res_login.get_json().get("access_token")
headers_carlos = {"Authorization": f"Bearer {token_carlos}"}

print("\n--- Ejecutando 4 llamadas consecutivas a GET /api/user/addresses con carlos@plomaap.com ---")
for i in range(1, 5):
    res = client.get("/api/user/addresses", headers=headers_carlos)
    cnt_after = get_db_address_count()
    json_data = res.get_json()
    print(f"Llamada #{i}:")
    print(f"  - HTTP Status: {res.status_code}")
    print(f"  - Body: {json_data}")
    print(f"  - Conteo user_addresses en BD: {cnt_after}")
    assert res.status_code == 200
    assert json_data.get("success") is True
    assert json_data.get("addresses") == []
    assert cnt_after == 6, f"ERROR: La llamada #{i} insertó datos en user_addresses (conteo: {cnt_after})"

print("\n[OK] Confirmado: 4 llamadas seguidas mantuvieron el conteo en exactamente 6 filas.")

# Now test with a user that DOES have saved addresses:
# user_id 14 or user_id 1
# Let's test with a generated token for user_id 1 (Andrés)
from app.utils.jwt_utils import generate_token
with app.app_context():
    token_user1 = generate_token(1)
headers_user1 = {"Authorization": f"Bearer {token_user1}"}

print("\n--- Probando GET /api/user/addresses con usuario 1 (tiene 2 direcciones) ---")
res_u1 = client.get("/api/user/addresses", headers=headers_user1)
cnt_u1 = get_db_address_count()
json_u1 = res_u1.get_json()
print(f"Status: {res_u1.status_code}")
print(f"Direcciones retornadas: {len(json_u1.get('addresses', []))}")
for addr in json_u1.get('addresses', []):
    print(f"  - [{addr.get('label')}] {addr.get('address')} (default: {addr.get('is_default')})")
print(f"Conteo user_addresses en BD: {cnt_u1}")
assert res_u1.status_code == 200
assert len(json_u1.get("addresses", [])) == 2
assert cnt_u1 == 6

# Now test sequence status
conn = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pw, dbname="plomapp")
cur = conn.cursor()
cur.execute("SELECT last_value, is_called FROM public.user_addresses_id_seq;")
last_val, is_called = cur.fetchone()
print(f"\n[*] Estado secuencia user_addresses_id_seq: last_value={last_val}, is_called={is_called}")
conn.close()

# Verify all baseline tables in Docker (5434)
print("\n--- Verificando todas las tablas en Docker (5434) ---")
conn = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pw, dbname="plomapp")
cur = conn.cursor()
baseline = {
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
}
for tbl, exp in baseline.items():
    cur.execute(f"SELECT COUNT(*) FROM public.{tbl};")
    c = cur.fetchone()[0]
    print(f"  - public.{tbl:<22}: {c:2d} (esperado: {exp:2d}) -> {'OK' if c == exp else 'ERROR'}")
    assert c == exp
conn.close()

# Verify MySQL untouched
print("\n--- Verificando MySQL (puerto 3306) ---")
conn_my = pymysql.connect(
    host=os.getenv("MYSQL_HOST", "localhost"),
    user=os.getenv("MYSQL_USER", "root"),
    password=os.getenv("MYSQL_PASSWORD", ""),
    port=int(os.getenv("MYSQL_PORT", 3306)),
    database=os.getenv("MYSQL_DB", "plomapp")
)
with conn_my.cursor() as cur:
    cur.execute("SHOW TABLES;")
    my_tables = cur.fetchall()
    total_my = 0
    for t in my_tables:
        cur.execute(f"SELECT COUNT(*) FROM `{t[0]}`;")
        total_my += cur.fetchone()[0]
    print(f"MySQL: {len(my_tables)} tablas, {total_my} registros (100% INTACTO).")
    assert len(my_tables) == 19 and total_my == 139
conn_my.close()

print("\n" + "=" * 65)
print("TODAS LAS VERIFICACIONES DEL FIX PASARON EXITOSAMENTE (100%)")
print("=" * 65)
