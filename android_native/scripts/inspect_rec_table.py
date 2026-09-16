import psycopg2
import os
import json
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
cur.execute("SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name = 'recommendation_events' ORDER BY ordinal_position")
rows = cur.fetchall()
print("RECOMMENDATION_EVENTS COLUMNS:")
for r in rows:
    print(f"  {r[0]}: {r[1]} (nullable: {r[2]})")

cur.execute("SELECT COUNT(*) FROM recommendation_events")
print("Total rows:", cur.fetchone()[0])
conn.close()
