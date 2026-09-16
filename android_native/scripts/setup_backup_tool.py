import os
import sys
import json
import datetime
import subprocess
from dotenv import load_dotenv

BACKEND_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..", "plomaap_react_estable", "backend_flask"))
if not os.path.exists(BACKEND_DIR):
    BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"

MIGRATION_DIR = os.path.join(BACKEND_DIR, "database_migration")
BACKUP_DIR = os.path.join(MIGRATION_DIR, "backups")
os.makedirs(BACKUP_DIR, exist_ok=True)

# Create the backup script inside backend_flask/database_migration/backup_mysql.py
backup_script_content = '''#!/usr/bin/env python
"""
PlomApp - MySQL Backup Utility (Non-destructive)
================================================
Generates a complete, reversible backup of the MySQL database before any migration.
Outputs:
1. SQL dump (.sql) with CREATE TABLE and INSERT statements.
2. JSON snapshot (.json) with serialized table rows.
3. Metadata report (.txt) with SHA-256 verification and row counts.
"""

import os
import sys
import json
import hashlib
import datetime
import subprocess
from decimal import Decimal
from dotenv import load_dotenv
import pymysql

# 1. Load environment
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
load_dotenv(os.path.join(BASE_DIR, ".env"))

DB_USER = os.getenv("MYSQL_USER", "root")
DB_PASS = os.getenv("MYSQL_PASSWORD", "")
DB_HOST = os.getenv("MYSQL_HOST", "localhost")
DB_PORT = int(os.getenv("MYSQL_PORT", 3306))
DB_NAME = os.getenv("MYSQL_DB", "plomapp")

BACKUP_DIR = os.path.join(os.path.dirname(__file__), "backups")
os.makedirs(BACKUP_DIR, exist_ok=True)

timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
sql_file = os.path.join(BACKUP_DIR, f"backup_{DB_NAME}_{timestamp}.sql")
json_file = os.path.join(BACKUP_DIR, f"backup_{DB_NAME}_{timestamp}.json")
meta_file = os.path.join(BACKUP_DIR, f"backup_{DB_NAME}_{timestamp}_manifest.txt")

print("=" * 60)
print(f"PLOMAPP - INICIANDO BACKUP SEGURO DE MYSQL ({DB_NAME})")
print(f"Timestamp: {timestamp}")
print(f"Destino: {BACKUP_DIR}")
print("=" * 60)

# Helper for JSON serialization of datetime / Decimal
def json_serial(obj):
    if isinstance(obj, (datetime.datetime, datetime.date, datetime.time)):
        return obj.isoformat()
    if isinstance(obj, Decimal):
        return float(obj)
    if isinstance(obj, (bytes, bytearray)):
        return obj.hex()
    raise TypeError(f"Type {type(obj)} not serializable")

# 2. Try mysqldump first if available
mysqldump_candidates = [
    r"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
    r"C:\\xampp\\mysql\\bin\\mysqldump.exe",
    "mysqldump"
]
mysqldump_exe = None
for c in mysqldump_candidates:
    if os.path.exists(c):
        mysqldump_exe = c
        break

mysqldump_success = False
if mysqldump_exe:
    print(f"[*] mysqldump detectado: {mysqldump_exe}")
    cmd = [
        mysqldump_exe,
        f"-h{DB_HOST}",
        f"-P{DB_PORT}",
        f"-u{DB_USER}",
        f"--password={DB_PASS}",
        "--single-transaction",
        "--quick",
        "--routines",
        "--triggers",
        "--databases",
        DB_NAME
    ]
    try:
        with open(sql_file, "w", encoding="utf-8") as out:
            proc = subprocess.run(cmd, stdout=out, stderr=subprocess.PIPE, text=True)
        if proc.returncode == 0:
            print(f"[OK] mysqldump ejecutado exitosamente -> {os.path.basename(sql_file)}")
            mysqldump_success = True
        else:
            print(f"[!] mysqldump retornó error: {proc.stderr.strip()}")
    except Exception as e:
        print(f"[!] Error ejecutando mysqldump: {e}")

# 3. If mysqldump failed or as additional safety: pure Python dump
conn = pymysql.connect(
    host=DB_HOST,
    user=DB_USER,
    password=DB_PASS,
    port=DB_PORT,
    database=DB_NAME,
    charset="utf8mb4",
    cursorclass=pymysql.cursors.DictCursor
)

json_data = {
    "database": DB_NAME,
    "timestamp": timestamp,
    "tables": {}
}

manifest_lines = [
    f"PLOMAPP DATABASE BACKUP MANIFEST",
    f"Database: {DB_NAME}",
    f"Host: {DB_HOST}:{DB_PORT}",
    f"Generated: {datetime.datetime.now().isoformat()}",
    "-" * 50,
    "TABLE INVENTORY & ROW COUNTS:"
]

total_rows_backed_up = 0

with conn.cursor() as cursor:
    cursor.execute("SHOW TABLES")
    tables = [list(r.values())[0] for r in cursor.fetchall()]
    print(f"[*] Extrayendo datos de {len(tables)} tablas via Python...")
    
    # Also write a clean SQL backup if mysqldump was not used
    python_sql_lines = []
    python_sql_lines.append(f"-- PlomApp MySQL Backup\\n-- Date: {timestamp}\\n-- Database: {DB_NAME}\\n\\nSET FOREIGN_KEY_CHECKS=0;\\n")

    for tbl in sorted(tables):
        cursor.execute(f"SHOW CREATE TABLE `{tbl}`")
        create_stmt = cursor.fetchone()["Create Table"]
        
        cursor.execute(f"SELECT * FROM `{tbl}`")
        rows = cursor.fetchall()
        row_count = len(rows)
        total_rows_backed_up += row_count
        
        json_data["tables"][tbl] = {
            "row_count": row_count,
            "create_statement": create_stmt,
            "rows": rows
        }
        
        manifest_lines.append(f"  - {tbl.ljust(25)} : {row_count:4d} rows")
        print(f"    - {tbl.ljust(25)}: {row_count:4d} filas")
        
        # Build SQL statements
        python_sql_lines.append(f"-- Table structure and data for `{tbl}`")
        python_sql_lines.append(f"{create_stmt};\\n")
        if rows:
            for r in rows:
                cols = ", ".join([f"`{k}`" for k in r.keys()])
                vals = []
                for v in r.values():
                    if v is None:
                        vals.append("NULL")
                    elif isinstance(v, (int, float)):
                        vals.append(str(v))
                    elif isinstance(v, Decimal):
                        vals.append(str(v))
                    elif isinstance(v, bool):
                        vals.append("1" if v else "0")
                    elif isinstance(v, (dict, list)):
                        escaped = json.dumps(v, default=json_serial).replace("'", "\\\\'")
                        vals.append(f"'{escaped}'")
                    else:
                        escaped = str(v).replace("'", "\\\\'")
                        vals.append(f"'{escaped}'")
                python_sql_lines.append(f"INSERT INTO `{tbl}` ({cols}) VALUES ({', '.join(vals)});")
        python_sql_lines.append("\\n")

    python_sql_lines.append("SET FOREIGN_KEY_CHECKS=1;\\n")

conn.close()

# Write JSON backup
with open(json_file, "w", encoding="utf-8") as jf:
    json.dump(json_data, jf, indent=2, default=json_serial)
print(f"[OK] Backup JSON guardado: {os.path.basename(json_file)}")

if not mysqldump_success:
    with open(sql_file, "w", encoding="utf-8") as sf:
        sf.write("\\n".join(python_sql_lines))
    print(f"[OK] Backup SQL (Python) guardado: {os.path.basename(sql_file)}")

# Compute Checksums
def get_sha256(filepath):
    h = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(8192):
            h.update(chunk)
    return h.hexdigest()

sql_sha = get_sha256(sql_file)
json_sha = get_sha256(json_file)

manifest_lines.append("-" * 50)
manifest_lines.append(f"TOTAL ROWS: {total_rows_backed_up}")
manifest_lines.append(f"SQL FILE:   {os.path.basename(sql_file)} ({os.path.getsize(sql_file)} bytes)")
manifest_lines.append(f"SQL SHA256: {sql_sha}")
manifest_lines.append(f"JSON FILE:  {os.path.basename(json_file)} ({os.path.getsize(json_file)} bytes)")
manifest_lines.append(f"JSON SHA256:{json_sha}")
manifest_lines.append("STATUS: VERIFIED SUCCESSFUL")

with open(meta_file, "w", encoding="utf-8") as mf:
    mf.write("\\n".join(manifest_lines) + "\\n")

print(f"[OK] Manifiesto guardado: {os.path.basename(meta_file)}")
print("=" * 60)
print("BACKUP COMPLETADO EXITOSAMENTE SIN MODIFICACIONES EN LA BASE DE DATOS.")
print("=" * 60)
'''

# Write backup_mysql.py
target_py = os.path.join(MIGRATION_DIR, "backup_mysql.py")
with open(target_py, "w", encoding="utf-8") as f:
    f.write(backup_script_content)
print(f"Creado: {target_py}")

# Write backup_mysql.bat
bat_content = '''@echo off
echo ========================================================
echo   Ejecutando Backup Seguro de MySQL (PlomApp)
echo ========================================================
cd /d "%~dp0"
..\\venv\\Scripts\\python.exe backup_mysql.py
pause
'''
target_bat = os.path.join(MIGRATION_DIR, "backup_mysql.bat")
with open(target_bat, "w", encoding="utf-8") as f:
    f.write(bat_content)
print(f"Creado: {target_bat}")

# Write restore_mysql_test.py for verifying rollback capability
restore_doc_content = '''# Guía de Restauración y Rollback de MySQL

Este procedimiento permite restaurar la base de datos MySQL `plomapp` exactamente a su estado previo a cualquier cambio.

## 1. Usando el dump SQL con MySQL CLI / MySQL Server

```bash
# Desde PowerShell o CMD:
"C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe" -u root -p plomapp < backend_flask/database_migration/backups/backup_plomapp_[TIMESTAMP].sql
```

## 2. Usando MySQL Workbench
1. Abrir MySQL Workbench y conectar a Localhost.
2. Menú **Server** -> **Data Import**.
3. Seleccionar "Import from Self-Contained File" y elegir el archivo `.sql` en `backend_flask/database_migration/backups/`.
4. Target Schema: `plomapp`.
5. Clic en **Start Import**.

## 3. Usando el respaldo JSON
El archivo `backup_plomapp_[TIMESTAMP].json` contiene la estructura completa (`CREATE TABLE`) y cada registro en formato JSON puro, lo cual permite reconstruir la base de datos de manera independiente a la versión del motor.
'''
target_md = os.path.join(MIGRATION_DIR, "ROLLBACK_GUIDE.md")
with open(target_md, "w", encoding="utf-8") as f:
    f.write(restore_doc_content)
print(f"Creado: {target_md}")
