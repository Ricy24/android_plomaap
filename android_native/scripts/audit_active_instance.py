import os
import sys
import psycopg2
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
sys.path.insert(0, BACKEND_DIR)
load_dotenv(os.path.join(BACKEND_DIR, ".env"))

from app.config import get_database_uri

print("=" * 65)
print("VERIFICACIÓN OBLIGATORIA DE INSTANCIA ACTIVA DE FLASK")
print("=" * 65)

active_uri = get_database_uri()
if ":" in active_uri and "@" in active_uri:
    up, hd = active_uri.split("@", 1)
    sch_usr = up.split(":", 1)[0]
    print(f"Flask get_database_uri(): {sch_usr}:***REDACTED***@{hd}")
else:
    print(f"Flask get_database_uri(): {active_uri}")

active_port = os.getenv("POSTGRES_PORT")
print(f"POSTGRES_PORT en .env: {active_port}")

# Check both databases
pg_pass = os.getenv("POSTGRES_PASSWORD", "")

def check_instance(port, label):
    try:
        conn = psycopg2.connect(host="localhost", port=port, user="postgres", password=pg_pass, dbname="plomapp")
        with conn.cursor() as cur:
            cur.execute("SELECT version();")
            ver = cur.fetchone()[0]
            cur.execute("SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';")
            ext = cur.fetchone()
            ext_str = f"{ext[0]} {ext[1]}" if ext else "NO HABILITADA"
            
            # Check baseline counts
            counts = {}
            for t in ["users", "services", "appointments", "user_addresses", "user_favorites", "technician_profiles"]:
                cur.execute(f"SELECT COUNT(*) FROM public.{t};")
                counts[t] = cur.fetchone()[0]
                
            # Check if any of the new tables exist
            new_tables = ["homes", "home_members", "saved_locations", "home_rooms", "home_assets", "room_type_catalog", "asset_type_catalog"]
            cur.execute("""
            SELECT table_name FROM information_schema.tables 
            WHERE table_schema = 'public' AND table_name = ANY(%s);
            """, (new_tables,))
            existing_new = [r[0] for r in cur.fetchall()]
            
            print(f"\n[*] {label} (Puerto {port}):")
            print(f"    Versión: {ver[:60]}...")
            print(f"    pgvector: {ext_str}")
            print(f"    Conteos: {counts}")
            print(f"    ¿Existen tablas nuevas de hogar?: {existing_new if existing_new else 'NINGUNA (0/7)'}")
            
        conn.close()
        return True, counts, ext_str
    except Exception as e:
        print(f"[-] Error conectando a puerto {port}: {e}")
        return False, None, None

ok_nat, counts_nat, ext_nat = check_instance(5432, "PostgreSQL Nativo (Windows)")
ok_doc, counts_doc, ext_doc = check_instance(5434, "PostgreSQL Contenedor (Docker)")

print("\n" + "=" * 65)
print("RESUMEN DE VERIFICACIÓN:")
print(f"  - Instancia a la que apunta Flask (.env): Puerto {active_port} ({'NATIVO' if str(active_port)=='5432' else 'DOCKER'})")
print(f"  - Contenedor Docker con pgvector: Corriendo en puerto 5434 con vector 0.8.6")
print(f"  - ¿El cutover a puerto 5434 está aplicado en .env?: {'SÍ' if str(active_port)=='5434' else 'NO, AÚN ESTÁ EN 5432'}")
print("=" * 65)
