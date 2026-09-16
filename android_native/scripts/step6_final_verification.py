import os
import sys
import psycopg2
import pymysql
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)

pg_pw = os.getenv("POSTGRES_PASSWORD", "")
mysql_pw = os.getenv("MYSQL_PASSWORD", "")

print("=" * 70)
print("FASE 7 - PASO 6: VERIFICACIÓN DE INTEGRIDAD FINAL DE TODAS LAS BASES")
print("=" * 70)

# 1. Docker PostgreSQL (puerto 5434)
print("\n--- 1. Docker PostgreSQL (puerto 5434) ---")
conn_docker = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pg_pw, dbname="plomapp")
cur_d = conn_docker.cursor()

expected_docker = {
    'users': 14,
    'services': 4,
    'appointments': 10,
    'user_addresses': 6,
    'user_favorites': 2,
    'technician_profiles': 2,
    'devices': 0,
    'webauthn_credentials': 0,
    'room_type_catalog': 7,
    'asset_type_catalog': 8,
    'homes': 0,
    'home_members': 0,
    'saved_locations': 0,
    'home_rooms': 0,
    'home_assets': 0,
    'profile_attributes': 0,
    'app_events': 0,
    'recommendation_events': 0
}

all_docker_ok = True
for table, exp_count in expected_docker.items():
    cur_d.execute(f"SELECT COUNT(*) FROM public.{table};")
    actual = cur_d.fetchone()[0]
    status = "OK" if actual == exp_count else "MISMATCH"
    if status != "OK":
        all_docker_ok = False
    print(f"  [{status}] {table:<25}: {actual} (esperado: {exp_count})")

conn_docker.close()
assert all_docker_ok, "Falla en verificación de Docker PostgreSQL"

# 2. Native PostgreSQL (puerto 5432)
print("\n--- 2. Native PostgreSQL (puerto 5432 - Hot Standby) ---")
try:
    conn_native = psycopg2.connect(host="localhost", port=5432, user="postgres", password=pg_pw, dbname="plomapp")
    cur_n = conn_native.cursor()
    native_tables = ['users', 'services', 'appointments', 'user_addresses', 'user_favorites', 'technician_profiles']
    expected_native = {'users': 14, 'services': 4, 'appointments': 10, 'user_addresses': 6, 'user_favorites': 2, 'technician_profiles': 2}
    all_native_ok = True
    for table, exp_count in expected_native.items():
        cur_n.execute(f"SELECT COUNT(*) FROM public.{table};")
        actual = cur_n.fetchone()[0]
        status = "OK" if actual == exp_count else "MISMATCH"
        if status != "OK":
            all_native_ok = False
        print(f"  [{status}] {table:<25}: {actual} (esperado: {exp_count})")
    conn_native.close()
    assert all_native_ok, "Falla en verificación de Native PostgreSQL"
    print("  -> PostgreSQL Nativo (5432) activo e intacto.")
except Exception as e:
    print(f"  [ERROR] Verificando PostgreSQL Nativo: {e}")
    sys.exit(1)

# 3. MySQL (puerto 3306)
print("\n--- 3. MySQL Legacy (puerto 3306) ---")
try:
    conn_mysql = pymysql.connect(host="localhost", port=3306, user="root", password=mysql_pw, database="plomapp")
    cur_m = conn_mysql.cursor()
    cur_m.execute("SHOW TABLES;")
    m_tables = [row[0] for row in cur_m.fetchall()]
    total_m_rows = 0
    for t in m_tables:
        cur_m.execute(f"SELECT COUNT(*) FROM `{t}`;")
        cnt = cur_m.fetchone()[0]
        total_m_rows += cnt
    conn_mysql.close()
    print(f"  [OK] MySQL tablas encontradas: {len(m_tables)} (esperado: 19)")
    print(f"  [OK] MySQL total registros:    {total_m_rows} (esperado: 139)")
    assert len(m_tables) == 19, f"MySQL tiene {len(m_tables)} tablas"
    assert total_m_rows == 139, f"MySQL tiene {total_m_rows} registros"
    print("  -> MySQL legacy completamente intacto (19 tablas, 139 registros).")
except Exception as e:
    print(f"  [ERROR] Verificando MySQL: {e}")
    sys.exit(1)

print("\n" + "=" * 70)
print("TODAS LAS VERIFICACIONES DE INTEGRIDAD PASARON AL 100%")
print("=" * 70)
