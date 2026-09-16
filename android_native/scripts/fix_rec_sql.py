import os

target = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\services\recommendation_service.py"
with open(target, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("JOIN home_assets ha ON ha.asset_type_id = ac.id", "JOIN home_assets ha ON ha.asset_type = ac.id")

with open(target, 'w', encoding='utf-8') as f:
    f.write(content)
print("Fixed SQL in recommendation_service.py")
