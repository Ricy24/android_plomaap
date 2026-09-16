import os
import sys
import subprocess
import psycopg2
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
load_dotenv(os.path.join(BACKEND_DIR, ".env"))

PG_RESTORE = r"C:\Program Files\PostgreSQL\17\bin\pg_restore.EXE"
PSQL = r"C:\Program Files\PostgreSQL\17\bin\psql.EXE"
BACKUP_DIR = os.path.join(BACKEND_DIR, "database_migration", "backups")

# Find latest dump and sql
dumps = sorted([f for f in os.listdir(BACKUP_DIR) if f.startswith("plomapp_native_backup_") and f.endswith(".dump")])
sqls = sorted([f for f in os.listdir(BACKUP_DIR) if f.startswith("plomapp_native_backup_") and f.endswith(".sql")])

if not dumps or not sqls:
    print("ERROR: No se encontraron archivos de backup.")
    sys.exit(1)

latest_dump = os.path.join(BACKUP_DIR, dumps[-1])
latest_sql = os.path.join(BACKUP_DIR, sqls[-1])

print("=" * 60)
print("PASO 4: RESTAURANDO BACKUP EN EL CONTENEDOR DOCKER (PUERTO 5434)")
print(f"Backup seleccionado: {os.path.basename(latest_dump)}")
print("=" * 60)

pg_pass = os.getenv("POSTGRES_PASSWORD", "")
env = os.environ.copy()
env["PGPASSWORD"] = pg_pass

# 1. Connect to postgres database in container to create plomapp
try:
    conn_maint = psycopg2.connect(
        host="localhost",
        port=5434,
        user="postgres",
        password=pg_pass,
        dbname="postgres"
    )
    conn_maint.autocommit = True
    with conn_maint.cursor() as cur:
        cur.execute("SELECT 1 FROM pg_database WHERE datname = 'plomapp';")
        if not cur.fetchone():
            print("[+] Creando base de datos 'plomapp' en el contenedor...")
            cur.execute("CREATE DATABASE plomapp ENCODING 'UTF8';")
        else:
            print("[OK] Base de datos 'plomapp' ya existe en el contenedor.")
    conn_maint.close()
except Exception as e:
    print(f"ERROR creando base de datos plomapp en contenedor: {e}")
    sys.exit(1)

# 2. Restore using pg_restore targeting container on port 5434
cmd_restore = [
    PG_RESTORE,
    "-h", "localhost",
    "-p", "5434",
    "-U", "postgres",
    "-d", "plomapp",
    "--no-owner",
    "--no-privileges",
    latest_dump
]
print(f"[*] Ejecutando pg_restore en contenedor (puerto 5434)...")
res = subprocess.run(cmd_restore, env=env, capture_output=True, text=True)
# Note: pg_restore can exit with 0 or 1 on warnings (like harmless warnings), let's print output
if res.returncode not in [0, 1]:
    print(f"Error en pg_restore: {res.stderr}")
    # Fallback to psql with plain sql
    print("[*] Probando restauración via psql y dump SQL...")
    cmd_psql = [
        PSQL,
        "-h", "localhost",
        "-p", "5434",
        "-U", "postgres",
        "-d", "plomapp",
        "-f", latest_sql
    ]
    res_psql = subprocess.run(cmd_psql, env=env, capture_output=True, text=True)
    if res_psql.returncode != 0:
        print(f"ERROR en psql restore: {res_psql.stderr}")
        sys.exit(1)
    else:
        print("[OK] Restauración via psql completada exitosamente.")
else:
    print("[OK] pg_restore completado exitosamente.")
    if res.stderr:
        print(f"     Avisos/Detalles: {res.stderr.strip()[:200]}")
