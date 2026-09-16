import os

INIT_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\__init__.py"

with open(INIT_PATH, "r", encoding="utf-8") as f:
    code = f.read()

if "from app.routes.search import search_bp" not in code:
    code = code.replace(
        "from app.routes.embeddings import embeddings_bp",
        "from app.routes.embeddings import embeddings_bp\nfrom app.routes.search import search_bp"
    )

if "app.register_blueprint(search_bp)" not in code:
    code = code.replace(
        "app.register_blueprint(embeddings_bp)",
        "app.register_blueprint(embeddings_bp)\n    app.register_blueprint(search_bp)"
    )

with open(INIT_PATH, "w", encoding="utf-8") as f:
    f.write(code)

print("[OK] search_bp registrado exitosamente en app/__init__.py")
