import os

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"

print("--- CONFIGURACIÓN DE FLASK & SQLALCHEMY ---")
config_dir = os.path.join(BACKEND_DIR, "app", "config")
if os.path.exists(config_dir):
    for f in os.listdir(config_dir):
        if f.endswith(".py"):
            print(f"\nArchivo config: {f}")
            with open(os.path.join(config_dir, f), encoding="utf-8") as fp:
                for line in fp:
                    if any(k in line for k in ["SQLALCHEMY", "DATABASE", "DB_", "Config"]):
                        print("  ", line.strip())

print("\n--- MODELOS DECLARADOS EN app/database/models.py ---")
models_path = os.path.join(BACKEND_DIR, "app", "database", "models.py")
if os.path.exists(models_path):
    with open(models_path, encoding="utf-8") as fp:
        code = fp.read()
    print(f"Tamaño de models.py: {len(code.splitlines())} líneas")

print("\n--- REVISAR SI HAY OTROS MODELOS (EJ. TABLAS LEGACY) ---")
for root, dirs, files in os.walk(os.path.join(BACKEND_DIR, "app")):
    for f in files:
        if f.endswith(".py") and f != "models.py":
            path = os.path.join(root, f)
            with open(path, encoding="utf-8", errors="ignore") as fp:
                c = fp.read()
                if "solicitudes_servicio" in c or "usuarios" in c or "cotizaciones" in c:
                    print(f"Mención de tablas legacy en {os.path.relpath(path, BACKEND_DIR)}")
