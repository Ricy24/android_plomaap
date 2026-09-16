import subprocess
import os

PSQL = r"C:\Program Files\PostgreSQL\17\bin\psql.EXE"
env = os.environ.copy()
env["PGCONNECT_TIMEOUT"] = "2"

print("--- PROBANDO AUTENTICACIÓN SIN CONTRASEÑA (TRUST/SSPI) ---")
# 1. User Anrid (Windows user)
res = subprocess.run([PSQL, "-U", "Anrid", "-h", "localhost", "-p", "5432", "-w", "-c", "SELECT 1;"], capture_output=True, text=True, env=env)
print("Anrid @ localhost:", res.returncode, res.stderr.strip())

# 2. Local without host (named pipe / local socket on windows)
res = subprocess.run([PSQL, "-U", "postgres", "-w", "-c", "SELECT 1;"], capture_output=True, text=True, env=env)
print("postgres (local default):", res.returncode, res.stderr.strip())
