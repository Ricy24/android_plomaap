TARGET = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\appointments.py"

with open(TARGET, 'r', encoding='utf-8') as f:
    content = f.read()

target = "'total_price': float(total_price)"
replacement = "'total_price': float(service.base_price) if service.base_price else 0.0"

assert target in content, "target line not found in appointments.py"

new_content = content.replace(target, replacement, 1)

with open(TARGET, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("[OK] appointments.py actualizado con service.base_price exitosamente.")
