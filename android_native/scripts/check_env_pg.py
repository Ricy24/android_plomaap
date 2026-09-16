import os

print("--- CHEQUEANDO VARIABLES DE ENTORNO POSTGRES ---")
for k, v in os.environ.items():
    if any(p in k.lower() for p in ["pg", "postgres"]):
        print(f"  {k} = {v}")

# Also check backend .env
backend_env = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\.env"
if os.path.exists(backend_env):
    with open(backend_env, encoding="utf-8") as fp:
        for line in fp:
            if any(p in line.lower() for p in ["pg", "postgres"]):
                print(f"  [.env] {line.strip()}")
