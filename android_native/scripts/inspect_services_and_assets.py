import psycopg2, os, json
from dotenv import load_dotenv

load_dotenv(r"C:\Users\Anrid\plomaap_react_estable\backend_flask\.env")
conn = psycopg2.connect(
    host=os.getenv("POSTGRES_HOST", "localhost"),
    port=int(os.getenv("POSTGRES_PORT", 5434)),
    dbname=os.getenv("POSTGRES_DB", "plomapp"),
    user=os.getenv("POSTGRES_USER", "postgres"),
    password=os.getenv("POSTGRES_PASSWORD", "postgres")
)
cur = conn.cursor()
cur.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'services'")
print("SERVICES COLUMNS:", cur.fetchall())

cur.execute("SELECT * FROM services")
services = cur.fetchall()
print("\nSERVICES ROWS:")
for s in services:
    print(s)

cur.execute("SELECT id, name, code, category FROM asset_type_catalog")
assets = cur.fetchall()
print("\nASSET CATALOG:")
for a in assets:
    print(a)

cur.execute("SELECT id, name, email FROM users LIMIT 5")
users = cur.fetchall()
print("\nSAMPLE USERS:")
for u in users:
    print(u)

conn.close()
