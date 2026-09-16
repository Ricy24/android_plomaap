import os
import sys
import subprocess
import datetime
import hashlib
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
load_dotenv(os.path.join(BACKEND_DIR, ".env"))

PG_DUMP = r"C:\Program Files\PostgreSQL\17\bin\pg_dump.EXE"
BACKUP_DIR = os.path.join(BACKEND_DIR, "database_migration", "backups")
os.makedirs(BACKUP_DIR, exist_ok=True)

timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
dump_file = os.path.join(BACKUP_DIR, f"plomapp_native_backup_{timestamp}.dump")
sql_file = os.path.join(BACKUP_DIR, f"plomapp_native_backup_{timestamp}.sql")

pg_user = os.getenv("POSTGRES_USER", "postgres")
pg_pass = os.getenv("POSTGRES_PASSWORD", "")
pg_host = os.getenv("POSTGRES_HOST", "localhost")
pg_port = os.getenv("POSTGRES_PORT", "5432")
pg_db = os.getenv("POSTGRES_DB", "plomapp")

env = os.environ.copy()
env["PGPASSWORD"] = pg_pass

print("=" * 60)
print("PASO 2: EJECUTANDO BACKUP COMPLETO DE POSTGRESQL NATIVO")
print(f"Origen: {pg_host}:{pg_port}/{pg_db} ({pg_user})")
print(f"Timestamp: {timestamp}")
print("=" * 60)

# 1. Custom format dump (-Fc)
cmd_custom = [
    PG_DUMP,
    "-h", pg_host,
    "-p", pg_port,
    "-U", pg_user,
    "-d", pg_db,
    "-Fc",
    "-f", dump_file
]
res_custom = subprocess.run(cmd_custom, env=env, capture_output=True, text=True)
if res_custom.returncode != 0:
    print(f"ERROR en pg_dump (-Fc): {res_custom.stderr}")
    sys.exit(1)

# 2. Plain SQL dump (.sql)
cmd_sql = [
    PG_DUMP,
    "-h", pg_host,
    "-p", pg_port,
    "-U", pg_user,
    "-d", pg_db,
    "-f", sql_file
]
res_sql = subprocess.run(cmd_sql, env=env, capture_output=True, text=True)
if res_sql.returncode != 0:
    print(f"ERROR en pg_dump (.sql): {res_sql.stderr}")
    sys.exit(1)

def get_sha256(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        while chunk := f.read(8192):
            h.update(chunk)
    return h.hexdigest()

dump_size = os.path.getsize(dump_file)
sql_size = os.path.getsize(sql_file)

print(f"[OK] Custom dump generado: {os.path.basename(dump_file)}")
print(f"     Tamaño: {dump_size} bytes | SHA256: {get_sha256(dump_file)}")
print(f"[OK] Plain SQL dump generado: {os.path.basename(sql_file)}")
print(f"     Tamaño: {sql_size} bytes | SHA256: {get_sha256(sql_file)}")

if dump_size < 1000 or sql_size < 1000:
    print("ERROR: Archivos de backup vacíos o demasiado pequeños.")
    sys.exit(1)

print("\nBackup verificado exitosamente.")
