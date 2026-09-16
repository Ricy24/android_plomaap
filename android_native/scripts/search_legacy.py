import os

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"

print("Buscando menciones de tablas legacy en backend_flask:")
legacy_names = ["solicitudes_servicio", "usuarios", "perfiles", "cotizaciones", "diagnosticos", "garantias", "registros_chat", "resenas"]

for root, dirs, files in os.walk(BACKEND_DIR):
    if any(p in root for p in ["venv", ".git", "backups", "instance", "__pycache__"]):
        continue
    for f in files:
        if f.endswith((".py", ".sql", ".md")):
            path = os.path.join(root, f)
            with open(path, encoding="utf-8", errors="ignore") as fp:
                txt = fp.read()
                matches = [name for name in legacy_names if name in txt]
                if matches:
                    rel = os.path.relpath(path, BACKEND_DIR)
                    print(f"  En {rel}: {matches}")
