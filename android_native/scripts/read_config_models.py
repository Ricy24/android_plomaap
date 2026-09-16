import os

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"

config_file = os.path.join(BACKEND_DIR, "app", "config", "__init__.py")
print("=== app/config/__init__.py ===")
with open(config_file, encoding="utf-8") as fp:
    print(fp.read())

models_file = os.path.join(BACKEND_DIR, "app", "database", "models.py")
print("\n=== app/database/models.py ===")
with open(models_file, encoding="utf-8") as fp:
    print(fp.read())
