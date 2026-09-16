import os
import sys
import psycopg2
import subprocess
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
load_dotenv(os.path.join(BACKEND_DIR, ".env"))
pg_pass = os.getenv("POSTGRES_PASSWORD", "")

print("=" * 60)
print("PASO 6: HABILITAR EXTENSIÓN PGVECTOR EN EL CONTENEDOR (plomapp)")
print("=" * 60)

conn_docker = psycopg2.connect(
    host="localhost",
    port=5434,
    user="postgres",
    password=pg_pass,
    dbname="plomapp"
)
conn_docker.autocommit = True

with conn_docker.cursor() as cur:
    print("[*] Ejecutando: CREATE EXTENSION IF NOT EXISTS vector;")
    cur.execute("CREATE EXTENSION IF NOT EXISTS vector;")
    
    cur.execute("SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';")
    ext = cur.fetchone()
    print(f"[OK] Extensión confirmada:")
    print(f"     Nombre: {ext[0]}, Versión: {ext[1]}")

conn_docker.close()

# Install python dependency pgvector in venv
print("\n" + "=" * 60)
print("PASO 3: INSTALAR DEPENDENCIA PYTHON PGVECTOR")
print("=" * 60)

pip_exe = os.path.join(BACKEND_DIR, "venv", "Scripts", "pip.exe")
res_pip = subprocess.run([pip_exe, "install", "pgvector==0.3.6"], capture_output=True, text=True)
print(f"pip install pgvector exit code: {res_pip.returncode}")
if res_pip.returncode != 0:
    print(res_pip.stderr)
else:
    print(res_pip.stdout.strip())

# Add to requirements.txt if not present
req_path = os.path.join(BACKEND_DIR, "requirements.txt")
with open(req_path, "r", encoding="utf-8") as f:
    req_content = f.read()

if "pgvector" not in req_content:
    req_content += "\npgvector==0.3.6\n"
    with open(req_path, "w", encoding="utf-8") as f:
        f.write(req_content)
    print("[OK] pgvector==0.3.6 agregado a requirements.txt")
else:
    print("[OK] pgvector ya está en requirements.txt")
