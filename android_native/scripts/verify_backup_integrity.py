import os
import sys
import json
import pymysql
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
MIGRATION_DIR = os.path.join(BACKEND_DIR, "database_migration")
BACKUP_DIR = os.path.join(MIGRATION_DIR, "backups")

# Find latest JSON backup
json_backups = sorted([f for f in os.listdir(BACKUP_DIR) if f.endswith(".json")])
if not json_backups:
    print("ERROR: No se encontró archivo de backup JSON.")
    sys.exit(1)

latest_backup = os.path.join(BACKUP_DIR, json_backups[-1])
print(f"Verificando contra backup: {os.path.basename(latest_backup)}")

with open(latest_backup, encoding="utf-8") as fp:
    backup_data = json.load(fp)

# Connect to live MySQL in read-only mode
load_dotenv(os.path.join(BACKEND_DIR, ".env"))
conn = pymysql.connect(
    host=os.getenv("MYSQL_HOST", "localhost"),
    user=os.getenv("MYSQL_USER", "root"),
    password=os.getenv("MYSQL_PASSWORD", ""),
    port=int(os.getenv("MYSQL_PORT", 3306)),
    database=os.getenv("MYSQL_DB", "plomapp"),
    charset="utf8mb4",
    cursorclass=pymysql.cursors.DictCursor
)

print("\n" + "=" * 65)
print(f"{'TABLA':<25} | {'MYSQL EN VIVO':<14} | {'EN BACKUP':<12} | {'ESTADO':<10}")
print("=" * 65)

all_match = True
total_live = 0
total_backup = 0

with conn.cursor() as cursor:
    cursor.execute("SHOW TABLES")
    live_tables = set(list(r.values())[0] for r in cursor.fetchall())
    backup_tables = set(backup_data["tables"].keys())

    for tbl in sorted(live_tables | backup_tables):
        cursor.execute(f"SELECT COUNT(*) AS cnt FROM `{tbl}`")
        live_cnt = cursor.fetchone()["cnt"]
        backup_cnt = backup_data["tables"].get(tbl, {}).get("row_count", -1)
        
        status = "COINCIDE" if live_cnt == backup_cnt else "DISCREPANCIA"
        if status != "COINCIDE":
            all_match = False
            
        print(f"{tbl:<25} | {live_cnt:<14} | {backup_cnt:<12} | {status:<10}")
        total_live += live_cnt
        total_backup += backup_cnt

conn.close()

print("=" * 65)
print(f"{'TOTALES':<25} | {total_live:<14} | {total_backup:<12} | {'OK' if all_match else 'ERROR'}")
print("=" * 65)
print(f"¿Verificación 100% exacta? {'SÍ, EL BACKUP CORRESPONDE EXACTAMENTE' if all_match else 'NO'}")
