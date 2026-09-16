import os
import shutil

INIT_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\__init__.py"
BACKUP_PATH = INIT_PATH + ".bak_fase9"

if not os.path.exists(BACKUP_PATH):
    shutil.copy2(INIT_PATH, BACKUP_PATH)
    print(f"[OK] Backup creado en {BACKUP_PATH}")

with open(INIT_PATH, "r", encoding="utf-8") as f:
    code = f.read()

# Add import if not present
if "from app.routes.embeddings import embeddings_bp" not in code:
    code = code.replace(
        "from app.routes.onboarding import onboarding_bp",
        "from app.routes.onboarding import onboarding_bp\nfrom app.routes.embeddings import embeddings_bp"
    )

# Register blueprint if not present
if "app.register_blueprint(embeddings_bp)" not in code:
    code = code.replace(
        "app.register_blueprint(onboarding_bp)",
        "app.register_blueprint(onboarding_bp)\n    app.register_blueprint(embeddings_bp)"
    )

with open(INIT_PATH, "w", encoding="utf-8") as f:
    f.write(code)

print(f"[OK] app/__init__.py actualizado con embeddings_bp")
