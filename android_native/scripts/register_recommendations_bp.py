import os

target = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\__init__.py"
with open(target, 'r', encoding='utf-8') as f:
    content = f.read()

if "recommendations_bp" not in content:
    content = content.replace(
        "from app.routes.intelligence import intelligence_bp",
        "from app.routes.intelligence import intelligence_bp\nfrom app.routes.recommendations import recommendations_bp"
    )
    content = content.replace(
        "app.register_blueprint(intelligence_bp)",
        "app.register_blueprint(intelligence_bp)\n    app.register_blueprint(recommendations_bp, url_prefix='/api/recommendations')"
    )
    
    with open(target, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Registered recommendations_bp")
else:
    print("Already registered")
