import os

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"

print("--- MIGRACIONES EXISTENTES ---")
mig_dir = os.path.join(BACKEND_DIR, "migrations", "versions")
if os.path.exists(mig_dir):
    files = os.listdir(mig_dir)
    print(f"Archivos en migrations/versions: {files}")
else:
    print("No existe migrations/versions")

print("\n--- MODELOS EN APP/MODELS ---")
models_dir = os.path.join(BACKEND_DIR, "app", "models")
if os.path.exists(models_dir):
    for f in os.listdir(models_dir):
        if f.endswith(".py"):
            print(f"  - {f}")
            with open(os.path.join(models_dir, f), encoding="utf-8") as fp:
                lines = [l.strip() for l in fp if "class " in l and "(db.Model" in l]
                for l in lines:
                    print(f"      {l}")

print("\n--- SCRIPTS DE SEED / INIT ---")
for root, dirs, files in os.walk(BACKEND_DIR):
    if "venv" in root or ".git" in root:
        continue
    for f in files:
        if any(w in f.lower() for w in ["seed", "init", "populate", "create_db", "reset"]):
            print(f"  {os.path.relpath(os.path.join(root, f), BACKEND_DIR)}")
