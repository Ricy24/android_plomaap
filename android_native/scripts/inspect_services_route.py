TARGET_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\services.py"

with open(TARGET_PATH, "r", encoding="utf-8") as f:
    code = f.read()

print("Length:", len(code))
print(code[:1500])
