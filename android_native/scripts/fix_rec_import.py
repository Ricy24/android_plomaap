import os

target = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\recommendations.py"
with open(target, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("from app.utils.auth import token_required", "from app.middlewares.auth import token_required")

with open(target, 'w', encoding='utf-8') as f:
    f.write(content)
print("Fixed import in recommendations.py")
