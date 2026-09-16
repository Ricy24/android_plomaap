import subprocess
import os
import json

PG_CONFIG = r"C:\Program Files\PostgreSQL\17\bin\pg_config.EXE"
PSQL = r"C:\Program Files\PostgreSQL\17\bin\psql.EXE"

print("=" * 60)
print("DIAGNÓSTICO POSTGRESQL 17")
print("=" * 60)

# 1. pg_config
if os.path.exists(PG_CONFIG):
    print("\n1. pg_config encontrado:")
    res = subprocess.run([PG_CONFIG, "--version"], capture_output=True, text=True)
    print("   Version:", res.stdout.strip())
    
    res = subprocess.run([PG_CONFIG, "--sharedir"], capture_output=True, text=True)
    sharedir = res.stdout.strip()
    print("   Sharedir:", sharedir)
    
    ext_dir = os.path.join(sharedir, "extension")
    if os.path.exists(ext_dir):
        print("   Directorio extensiones:", ext_dir)
        ext_files = [f for f in os.listdir(ext_dir) if f.endswith(".control")]
        print(f"   Total extensiones instaladas en disco: {len(ext_files)}")
        has_vector = any("vector" in f for f in ext_files)
        print(f"   ¿pgvector presente en extensiones? {'SÍ' if has_vector else 'NO'}")
        if has_vector:
            vector_files = [f for f in ext_files if "vector" in f]
            print(f"   Archivos de control vector: {vector_files}")
else:
    print("pg_config no encontrado en ruta esperada.")

# 2. Test psql connection
# Try connecting without password (trust/SSPI) or check prompt
print("\n2. Verificando conexión local con psql:")
try:
    # Set env var PGDATABASE=postgres, PGUSER=postgres, PGCONNECT_TIMEOUT=3
    env = os.environ.copy()
    env["PGCONNECT_TIMEOUT"] = "3"
    # Run with -l (list databases) without interactive prompt
    res = subprocess.run(
        [PSQL, "-U", "postgres", "-h", "localhost", "-p", "5432", "-w", "-c", "SELECT version();"],
        capture_output=True,
        text=True,
        env=env,
        timeout=5
    )
    print("   Código salida:", res.returncode)
    print("   STDOUT:", res.stdout.strip())
    print("   STDERR:", res.stderr.strip())
except subprocess.TimeoutExpired:
    print("   Timeout al conectar a psql (posiblemente esperando contraseña en modo interactivo).")
except Exception as e:
    print("   Error al ejecutar psql:", e)
