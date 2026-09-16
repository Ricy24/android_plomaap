import os

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"

config_file = os.path.join(BACKEND_DIR, "app", "config", "__init__.py")
print("=== app/config/__init__.py (Lines 1-80) ===")
with open(config_file, encoding="utf-8") as fp:
    lines = fp.readlines()
    for i, l in enumerate(lines[:80]):
        print(f"{i+1}: {l.rstrip()}")

models_file = os.path.join(BACKEND_DIR, "app", "database", "models.py")
print("\n=== app/database/models.py (Lines 1-80) ===")
with open(models_file, encoding="utf-8") as fp:
    lines = fp.readlines()
    for i, l in enumerate(lines[:80]):
        print(f"{i+1}: {l.rstrip()}")
