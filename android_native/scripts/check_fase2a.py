import os
import sys
import psycopg2
import pymysql
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
load_dotenv(os.path.join(BACKEND_DIR, ".env"))

print("=" * 60)
print("PASO 1: AUDITORÍA PREVIA Y VERIFICACIÓN")
print("=" * 60)

# 1. Check PostgreSQL connection using .env variables
pg_user = os.getenv("POSTGRES_USER", "postgres")
pg_pass = os.getenv("POSTGRES_PASSWORD", "")
pg_host = os.getenv("POSTGRES_HOST", "localhost")
pg_port = int(os.getenv("POSTGRES_PORT", 5432))
pg_db = os.getenv("POSTGRES_DB", "plomapp")

print(f"[*] Conectando a PostgreSQL ({pg_host}:{pg_port}/{pg_db}) como '{pg_user}'...")
pg_conn = psycopg2.connect(
    host=pg_host,
    port=pg_port,
    user=pg_user,
    password=pg_pass,
    dbname=pg_db
)

with pg_conn.cursor() as cur:
    cur.execute("SELECT version();")
    pg_version = cur.fetchone()[0]
    print(f"[OK] Versión PostgreSQL: {pg_version}")

    # Verify counts of core modern tables
    expected_counts = {
        "users": 14,
        "services": 4,
        "appointments": 10,
        "user_addresses": 6,
        "user_favorites": 2,
        "technician_profiles": 2,
        "devices": 0,
        "webauthn_credentials": 0
    }
    print("\n[*] Verificando registros en PostgreSQL (public.*):")
    pg_intact = True
    for tbl, exp in expected_counts.items():
        cur.execute(f"SELECT COUNT(*) FROM public.{tbl};")
        cnt = cur.fetchone()[0]
        match = (cnt == exp)
        print(f"  - public.{tbl:<22}: {cnt:2d} (esperado: {exp:2d}) -> {'OK' if match else 'DISCREPANCIA'}")
        if not match:
            pg_intact = False

# 2. Verify MySQL remains 100% untouched
my_user = os.getenv("MYSQL_USER", "root")
my_pass = os.getenv("MYSQL_PASSWORD", "")
my_host = os.getenv("MYSQL_HOST", "localhost")
my_port = int(os.getenv("MYSQL_PORT", 3306))
my_db = os.getenv("MYSQL_DB", "plomapp")

print(f"\n[*] Conectando a MySQL ({my_host}:{my_port}/{my_db}) para verificar integridad...")
my_conn = pymysql.connect(
    host=my_host,
    port=my_port,
    user=my_user,
    password=my_pass,
    database=my_db
)

with my_conn.cursor() as cur:
    cur.execute("SHOW TABLES;")
    my_tables = [r[0] for r in cur.fetchall()]
    total_my_rows = 0
    for t in my_tables:
        cur.execute(f"SELECT COUNT(*) FROM `{t}`;")
        total_my_rows += cur.fetchone()[0]
    print(f"[OK] MySQL Tablas: {len(my_tables)} (esperado: 19), Filas totales: {total_my_rows} (esperado: 139)")
    my_intact = (len(my_tables) == 19 and total_my_rows == 139)

my_conn.close()

# 3. Check requirements.txt
req_file = os.path.join(BACKEND_DIR, "requirements.txt")
with open(req_file, encoding="utf-8") as f:
    reqs = [l.strip() for l in f if l.strip() and not l.startswith("#")]
print(f"\n[*] Requirements.txt ({len(reqs)} paquetes):")
for r in reqs:
    if any(k in r.lower() for k in ["sql", "psycopg", "pg", "flask"]):
        print(f"  - {r}")

print("\n" + "=" * 60)
print("PASO 2: VERIFICACIÓN DE DISPONIBILIDAD DE PGVECTOR")
print("=" * 60)

with pg_conn.cursor() as cur:
    # Check if vector extension is already installed
    cur.execute("SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';")
    installed_ext = cur.fetchone()
    if installed_ext:
        print(f"[+] Extensión 'vector' ya está habilitada: Versión {installed_ext[1]}")
    else:
        print("[-] Extensión 'vector' NO está habilitada en pg_extension.")

    # Check if vector extension is available in pg_available_extensions
    cur.execute("SELECT name, default_version, comment FROM pg_available_extensions WHERE name = 'vector';")
    avail_ext = cur.fetchone()
    if avail_ext:
        print(f"[+] Extensión 'vector' está DISPONIBLE en el servidor:")
        print(f"    Nombre: {avail_ext[0]}, Versión por defecto: {avail_ext[1]}, Comentario: {avail_ext[2]}")
    else:
        print("[-] Extensión 'vector' NO está disponible en pg_available_extensions.")

pg_conn.close()
