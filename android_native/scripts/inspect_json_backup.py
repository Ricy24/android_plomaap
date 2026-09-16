import json
import os

BACKUP_FILE = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\database_migration\backups\backup_plomapp_20260909_125027.json"

with open(BACKUP_FILE, encoding="utf-8") as fp:
    data = json.load(fp)

print("=== INSPECCIÓN DE TABLAS Y TIPOS EN BACKUP JSON ===")
for tbl, info in data["tables"].items():
    rows = info.get("rows", [])
    print(f"\nTabla: {tbl} ({info['row_count']} filas)")
    if rows:
        sample = rows[0]
        print("  Columnas:", list(sample.keys()))
        for k, v in list(sample.items())[:5]:
            print(f"    {k}: {repr(v)[:50]} (tipo: {type(v).__name__})")
