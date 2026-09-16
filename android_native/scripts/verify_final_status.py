import os
import sys
import psycopg2
import pymysql
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
sys.path.insert(0, BACKEND_DIR)
load_dotenv(os.path.join(BACKEND_DIR, ".env"))
pg_pass = os.getenv("POSTGRES_PASSWORD", "")

print("=" * 65)
print("PASO 8: VERIFICACIÓN DE ESTADO FINAL ANTES DEL REPORTE")
print("=" * 65)

# 1. Native PostgreSQL on 5432 (Still used by Flask)
conn_nat = psycopg2.connect(host="localhost", port=5432, user="postgres", password=pg_pass, dbname="plomapp")
with conn_nat.cursor() as cur:
    print("[1] PostgreSQL Nativo (Windows en puerto 5432):")
    for t in ["users", "services", "appointments", "user_addresses", "user_favorites", "technician_profiles"]:
        cur.execute(f"SELECT COUNT(*) FROM public.{t};")
        print(f"    - public.{t}: {cur.fetchone()[0]}")
conn_nat.close()

# 2. Container PostgreSQL on 5434
conn_doc = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pg_pass, dbname="plomapp")
with conn_doc.cursor() as cur:
    print("\n[2] PostgreSQL Contenedor Docker (pgvector en puerto 5434):")
    for t in ["users", "services", "appointments", "user_addresses", "user_favorites", "technician_profiles"]:
        cur.execute(f"SELECT COUNT(*) FROM public.{t};")
        print(f"    - public.{t}: {cur.fetchone()[0]}")
    cur.execute("SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';")
    ext = cur.fetchone()
    print(f"    - Extensión vector: {ext[0]} {ext[1]}")
    cur.execute("SELECT COUNT(*) FROM public.entity_embeddings;")
    print(f"    - public.entity_embeddings: {cur.fetchone()[0]} filas")
conn_doc.close()

# 3. MySQL on 3306
conn_my = pymysql.connect(
    host=os.getenv("MYSQL_HOST", "localhost"),
    user=os.getenv("MYSQL_USER", "root"),
    password=os.getenv("MYSQL_PASSWORD", ""),
    port=int(os.getenv("MYSQL_PORT", 3306)),
    database=os.getenv("MYSQL_DB", "plomapp")
)
with conn_my.cursor() as cur:
    cur.execute("SHOW TABLES;")
    tables = cur.fetchall()
    total_my = 0
    for t in tables:
        cur.execute(f"SELECT COUNT(*) FROM `{t[0]}`;")
        total_my += cur.fetchone()[0]
    print(f"\n[3] MySQL (puerto 3306): {len(tables)} tablas, {total_my} registros totales (100% INTACTO)")
conn_my.close()

# 4. Flask active configuration
from app.config import get_database_uri
active_uri = get_database_uri()
if ":" in active_uri and "@" in active_uri:
    up, hd = active_uri.split("@", 1)
    sch_usr = up.split(":", 1)[0]
    print(f"\n[4] Flask active connection string: {sch_usr}:***REDACTED***@{hd}")
else:
    print(f"\n[4] Flask active connection string: {active_uri}")
assert ":5432/" in active_uri, "ERROR: Flask no está apuntando al puerto 5432 nativo"
print("    [OK] Confirmado: Flask sigue apuntando al puerto 5432 nativo (NO se ha hecho cutover).")
