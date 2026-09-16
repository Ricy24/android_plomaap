import os

INIT_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\__init__.py"

with open(INIT_PATH, "r", encoding="utf-8") as f:
    code = f.read()

if "from app.routes.intelligence import intelligence_bp" not in code:
    code = code.replace(
        "from app.routes.search import search_bp",
        "from app.routes.search import search_bp\nfrom app.routes.intelligence import intelligence_bp"
    )

if "app.register_blueprint(intelligence_bp)" not in code:
    code = code.replace(
        "app.register_blueprint(search_bp)",
        "app.register_blueprint(search_bp)\n    app.register_blueprint(intelligence_bp)"
    )

with open(INIT_PATH, "w", encoding="utf-8") as f:
    f.write(code)

print("[OK] intelligence_bp registrado exitosamente en app/__init__.py")
