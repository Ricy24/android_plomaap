import os
import psycopg2
from dotenv import load_dotenv

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)
pw = os.getenv("POSTGRES_PASSWORD", "")

conn = psycopg2.connect(host="localhost", port=5434, user="postgres", password=pw, dbname="plomapp")
cur = conn.cursor()
cur.execute("DELETE FROM public.appointments WHERE id = 14;")
print("Filas eliminadas:", cur.rowcount)
cur.execute("SELECT setval('public.appointments_id_seq', 13, true);")
conn.commit()
cur.execute("SELECT count(*) FROM public.appointments;")
print("Conteo final de appointments en Docker 5434:", cur.fetchone()[0])
conn.close()
