import os
import sys
import psycopg2
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
load_dotenv(os.path.join(BACKEND_DIR, ".env"))
pg_pass = os.getenv("POSTGRES_PASSWORD", "")

print("=" * 65)
print("PASO 5: VALIDACIÓN DE INTEGRIDAD LADO A LADO (NATIVO VS CONTENEDOR)")
print("=" * 65)

# 1. Connect to Native PG (5432)
conn_native = psycopg2.connect(
    host="localhost",
    port=5432,
    user="postgres",
    password=pg_pass,
    dbname="plomapp"
)

# 2. Connect to Docker PG (5434)
conn_docker = psycopg2.connect(
    host="localhost",
    port=5434,
    user="postgres",
    password=pg_pass,
    dbname="plomapp"
)

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

with conn_native.cursor() as cur_nat, conn_docker.cursor() as cur_doc:
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

print("=" * 65)
print(f"¿Integridad 100% idéntica? {'SÍ, CONTEOS EXACTOS' if all_match else 'NO, DISCREPANCIA'}")

conn_native.close()
conn_docker.close()

if not all_match:
    sys.exit(1)
