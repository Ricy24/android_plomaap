import os
import sys

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"

print("=" * 60)
print("AUDITORÍA DE BACKEND FLASK")
print("=" * 60)

print("\n1. ESTRUCTURA DE CARPETAS Y ARCHIVOS:")
for root, dirs, files in os.walk(BACKEND_DIR):
    rel = os.path.relpath(root, BACKEND_DIR)
    if rel.startswith("venv") or rel.startswith(".git") or rel.startswith("__pycache__"):
        continue
    # Do not traverse into venv or .git
    dirs[:] = [d for d in dirs if d not in ["venv", ".git", "__pycache__", ".pytest_cache"]]
    indent = "  " * rel.count(os.sep)
    if rel == ".":
        print("backend_flask/")
    else:
        print(f"{indent}{os.path.basename(root)}/")
    for f in files:
        if not f.endswith(".pyc"):
            print(f"{indent}  - {f}")

print("\n2. ARCHIVOS DE CONFIGURACIÓN Y ENTORNO:")
env_file = os.path.join(BACKEND_DIR, ".env")
if os.path.exists(env_file):
    print("Archivo .env encontrado:")
    with open(env_file, encoding="utf-8") as fp:
        for line in fp:
            line = line.strip()
            if line and not line.startswith("#"):
                key = line.split("=", 1)[0]
                # Redact sensitive values
                if any(s in key.lower() for s in ["password", "secret", "key", "token"]):
                    print(f"  {key}=***REDACTED***")
                elif "database" in key.lower() or "db" in key.lower() or "url" in key.lower():
                    # Redact password in URI if present
                    val = line.split("=", 1)[1] if "=" in line else ""
                    if "@" in val and ":" in val:
                        prefix, rest = val.split("://", 1) if "://" in val else ("", val)
                        user_pass, host_db = rest.split("@", 1)
                        user = user_pass.split(":", 1)[0]
                        print(f"  {key}={prefix}://{user}:***REDACTED***@{host_db}")
                    else:
                        print(f"  {key}={val}")
                else:
                    print(f"  {line}")
else:
    print("No se encontró archivo .env")

print("\n3. DEPENDENCIAS (requirements.txt / pip):")
req_file = os.path.join(BACKEND_DIR, "requirements.txt")
if os.path.exists(req_file):
    with open(req_file, encoding="utf-8") as fp:
        reqs = [l.strip() for l in fp if l.strip() and not l.startswith("#")]
        print(f"Total en requirements.txt: {len(reqs)}")
        for r in reqs:
            if any(db in r.lower() for db in ["sql", "mysql", "postgres", "psycopg", "pg", "alembic"]):
                print(f"  [DB Dep] {r}")
else:
    print("No requirements.txt")

print("\n4. MODELOS Y TABLAS ACTUALES EN SQLALCHEMY / MYSQL:")
sys.path.insert(0, BACKEND_DIR)
os.chdir(BACKEND_DIR)

from app import create_app
from app.database.extensions import db
from sqlalchemy import inspect, text

app = create_app()
with app.app_context():
    engine = db.engine
    inspector = inspect(engine)
    table_names = inspector.get_table_names()
    print(f"Tablas detectadas en base de datos ({len(table_names)}):")
    for t in sorted(table_names):
        cols = inspector.get_columns(t)
        col_summary = [f"{c['name']} ({c['type']})" for c in cols]
        fks = inspector.get_foreign_keys(t)
        fk_summary = [f"{fk['constrained_columns']} -> {fk['referred_table']}.{fk['referred_columns']}" for fk in fks]
        
        # Row count
        try:
            cnt = db.session.execute(text(f"SELECT COUNT(*) FROM `{t}`")).scalar()
        except Exception:
            cnt = "N/A"
            
        print(f"\n  Tabla: `{t}` | Filas: {cnt}")
        print(f"    Columnas ({len(cols)}): {', '.join(col_summary)}")
        if fk_summary:
            print(f"    Foreign Keys: {', '.join(fk_summary)}")
