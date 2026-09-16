import os

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
app_init = os.path.join(BACKEND_DIR, "app", "__init__.py")

print("=== app/__init__.py ===")
with open(app_init, encoding="utf-8") as fp:
    print(fp.read())
