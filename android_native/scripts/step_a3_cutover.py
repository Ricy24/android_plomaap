import os
import sys

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
env_path = os.path.join(BACKEND_DIR, ".env")

with open(env_path, "r", encoding="utf-8") as f:
    lines = f.readlines()

new_lines = []
for l in lines:
    if l.startswith("POSTGRES_PORT="):
        new_lines.append("POSTGRES_PORT=5434\n")
    elif l.startswith("DATABASE_URL="):
        # Update port 5432 to 5434 if present
        new_lines.append(l.replace(":5432/", ":5434/"))
    else:
        new_lines.append(l)

with open(env_path, "w", encoding="utf-8") as f:
    f.writelines(new_lines)

print("Paso A3: .env actualizado exitosamente con POSTGRES_PORT=5434")
