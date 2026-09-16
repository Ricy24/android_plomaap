import os
import sys
import shutil
import datetime
import psycopg2
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
load_dotenv(os.path.join(BACKEND_DIR, ".env"))

print("=" * 65)
print("PARTE A - PASO A1: VERIFICACIÓN FINAL PRE-CUTOVER")
print("=" * 65)

pg_pass = os.getenv("POSTGRES_PASSWORD", "")

conn_nat = psycopg2.connect(host="localhost", port=5432, user="postgres", password=pg_pass, dbname="plomapp")
conn_doc = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pg_pass, dbname="plomapp")

modern_tables = [
    "users", "services", "appointments", "user_addresses",
    "user_favorites", "technician_profiles", "devices", "webauthn_credentials"
]

legacy_tables = [
    "usuarios", "perfiles", "solicitudes_servicio", "cotizaciones",
    "diagnosticos", "garantias", "pagos", "registros_chat",
    "resenas", "agendar_servicio", "materiales_cotizacion"
]

all_match = True
print(f"{'TABLA':<26} | {'NATIVO (5432)':<13} | {'DOCKER (5434)':<13} | {'ESTADO':<10}")
print("-" * 65)

with conn_nat.cursor() as cur_nat, conn_doc.cursor() as cur_doc:
    for t in sorted(modern_tables):
        cur_nat.execute(f"SELECT COUNT(*) FROM public.{t};")
        cnt_nat = cur_nat.fetchone()[0]
        cur_doc.execute(f"SELECT COUNT(*) FROM public.{t};")
        cnt_doc = cur_doc.fetchone()[0]
        match = (cnt_nat == cnt_doc)
        if not match:
            all_match = False
        print(f"public.{t:<20} | {cnt_nat:<13} | {cnt_doc:<13} | {'COINCIDE' if match else 'ERROR'}")

    print("-" * 65)
    for t in sorted(legacy_tables):
        cur_nat.execute(f"SELECT COUNT(*) FROM legacy.{t};")
        cnt_nat = cur_nat.fetchone()[0]
        cur_doc.execute(f"SELECT COUNT(*) FROM legacy.{t};")
        cnt_doc = cur_doc.fetchone()[0]
        match = (cnt_nat == cnt_doc)
        if not match:
            all_match = False
        print(f"legacy.{t:<20} | {cnt_nat:<13} | {cnt_doc:<13} | {'COINCIDE' if match else 'ERROR'}")

conn_nat.close()
conn_doc.close()

if not all_match:
    print("\n[!] ERROR: Discrepancia entre nativo y contenedor. Abortando cutover.")
    sys.exit(1)

print("\n[OK] Verificación A1 superada: 100% de conteos coinciden entre nativo y contenedor.")

print("\n" + "=" * 65)
print("PARTE A - PASO A2: BACKUP DE CONFIGURACIÓN (.env)")
print("=" * 65)

timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
env_path = os.path.join(BACKEND_DIR, ".env")
env_backup_path = os.path.join(BACKEND_DIR, f".env.backup_pre_cutover_{timestamp}")

shutil.copyfile(env_path, env_backup_path)
print(f"[OK] Backup creado: {os.path.basename(env_backup_path)}")

# Verify connection mechanism in Flask
print("\nConfirmación del mecanismo de conexión que usa Flask:")
print("  - Variable DATABASE_URL en .env:", os.getenv("DATABASE_URL"))
print("  - Variable DB_ENGINE en .env:", os.getenv("DB_ENGINE"))
print("  - Variable POSTGRES_PORT en .env actual:", os.getenv("POSTGRES_PORT"))
print("  - En app/config/__init__.py: get_database_uri() lee POSTGRES_PORT cuando DB_ENGINE=postgresql.")
