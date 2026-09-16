import os

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"

print("--- BUSCANDO db.Model EN APP ---")
for root, dirs, files in os.walk(os.path.join(BACKEND_DIR, "app")):
    for f in files:
        if f.endswith(".py"):
            path = os.path.join(root, f)
            with open(path, encoding="utf-8", errors="ignore") as fp:
                content = fp.read()
                if "db.Model" in content or "class " in content and "Model" in content:
                    rel = os.path.relpath(path, BACKEND_DIR)
                    print(f"En {rel}:")
                    for line in content.splitlines():
                        if line.strip().startswith("class "):
                            print(f"    {line.strip()}")
