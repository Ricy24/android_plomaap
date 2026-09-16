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

cur.execute("SELECT COUNT(*) FROM public.user_addresses;")
count = cur.fetchone()[0]
print(f"1. Conteo de user_addresses en Docker (5434): {count}")
assert count == 6, f"ERROR: user_addresses no tiene 6 filas, tiene {count}"

cur.execute("SELECT last_value, is_called FROM public.user_addresses_id_seq;")
last_val, is_called = cur.fetchone()
print(f"2. Estado secuencia: last_value={last_val}, is_called={is_called}")

# In postgresql, if last_value=6 and is_called=True, nextval will return 7.
# Or if last_value=7 and is_called=False, nextval will return 7.
# Let's verify what nextval returns in a test transaction and rollback:
cur.execute("BEGIN;")
cur.execute("SELECT nextval('public.user_addresses_id_seq');")
test_nv = cur.fetchone()[0]
print(f"3. Valor que generaría nextval(): {test_nv}")
conn.rollback()

# Ensure it stays at 6 with is_called=true so nextval is 7:
cur.execute("SELECT setval('public.user_addresses_id_seq', 6, true);")
conn.commit()

cur.execute("SELECT last_value, is_called FROM public.user_addresses_id_seq;")
last_val, is_called = cur.fetchone()
print(f"4. Secuencia confirmada: last_value={last_val}, is_called={is_called} (próximo nextval = 7)")

conn.close()
