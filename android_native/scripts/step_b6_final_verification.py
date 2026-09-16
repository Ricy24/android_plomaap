import os
import sys
import psycopg2
import pymysql
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)
pw = os.getenv("POSTGRES_PASSWORD", "")

print("=" * 65)
print("PASO B6: VERIFICACIÓN FINAL DE INTEGRIDAD (POST FASE 6)")
print("=" * 65)

# 1. Docker 5434
conn_doc = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pw, dbname="plomapp")
cur_doc = conn_doc.cursor()
expected_docker = {
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
    "recommendation_events": 0,
}

print("[1] Verificando tablas en Contenedor Docker (puerto 5434):")
all_docker_ok = True
for tbl, exp in expected_docker.items():
    cur_doc.execute(f"SELECT COUNT(*) FROM public.{tbl};")
    c = cur_doc.fetchone()[0]
    ok = (c == exp)
    if not ok: all_docker_ok = False
    print(f"  - public.{tbl:<22}: {c:2d} (esperado: {exp:2d}) -> {'OK' if ok else 'ERROR'}")
assert all_docker_ok, "ERROR: Discrepancia en Docker 5434"
conn_doc.close()

# 2. Native PG 5432
conn_nat = psycopg2.connect(host="localhost", port=5432, user="postgres", password=pw, dbname="plomapp")
cur_nat = conn_nat.cursor()
cur_nat.execute("SELECT COUNT(*) FROM public.users;")
nat_users = cur_nat.fetchone()[0]
cur_nat.execute("SELECT COUNT(*) FROM public.user_addresses;")
nat_addr = cur_nat.fetchone()[0]
cur_nat.execute("SELECT COUNT(*) FROM public.appointments;")
nat_apt = cur_nat.fetchone()[0]
print(f"\n[2] PostgreSQL Nativo (5432): users={nat_users}, addresses={nat_addr}, appointments={nat_apt} (100% INTACTO)")
assert nat_users == 14 and nat_addr == 6 and nat_apt == 10
conn_nat.close()

# 3. MySQL 3306
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
print(f"\n[3] MySQL (3306): {len(my_tables)} tablas, {total_my} registros (100% INTACTO)")
assert len(my_tables) == 19 and total_my == 139
conn_my.close()

print("\n" + "=" * 65)
print("TODAS LAS VERIFICACIONES DEL PASO B6 PASARON AL 100%")
print("=" * 65)
