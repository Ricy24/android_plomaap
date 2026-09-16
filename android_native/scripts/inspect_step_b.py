import os
import sys
import psycopg2
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)
pw = os.getenv("POSTGRES_PASSWORD", "")

conn = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pw, dbname="plomapp")
cur = conn.cursor()

print("=== ROOM TYPE CATALOG (7 rows) ===")
cur.execute("SELECT code, name, description, icon, display_order, is_active FROM public.room_type_catalog ORDER BY display_order;")
for r in cur.fetchall():
    print(r)

print("\n=== ASSET TYPE CATALOG (8 rows) ===")
cur.execute("SELECT code, name, category, icon, default_maintenance_months, display_order, is_active FROM public.asset_type_catalog ORDER BY display_order;")
for r in cur.fetchall():
    print(r)

print("\n=== CONSTRAINTS ON NEW TABLES ===")
cur.execute("""
SELECT conname, contype, pg_get_constraintdef(c.oid)
FROM pg_constraint c
JOIN pg_namespace n ON n.oid = c.connamespace
JOIN pg_class cl ON cl.oid = c.conrelid
WHERE n.nspname = 'public' AND cl.relname IN ('room_type_catalog', 'asset_type_catalog', 'homes', 'home_members', 'saved_locations', 'home_rooms', 'home_assets')
ORDER BY cl.relname, conname;
""")
for r in cur.fetchall():
    print(f"  - [{r[1]}] {r[0]}: {r[2]}")

print("\n=== INDEXES ON NEW TABLES ===")
cur.execute("""
SELECT tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public' AND tablename IN ('room_type_catalog', 'asset_type_catalog', 'homes', 'home_members', 'saved_locations', 'home_rooms', 'home_assets')
ORDER BY tablename, indexname;
""")
for r in cur.fetchall():
    print(f"  - {r[0]}.{r[1]}: {r[2]}")

conn.close()
