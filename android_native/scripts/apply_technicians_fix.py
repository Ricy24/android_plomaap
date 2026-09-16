TARGET = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\technicians.py"

with open(TARGET, 'r', encoding='utf-8') as f:
    content = f.read()

target = "    if appointment.technician_id != user_id:"
replacement = "    if appointment.technician_id != int(user_id):"

assert target in content, "Target line not found in technicians.py"

new_content = content.replace(target, replacement, 1)

with open(TARGET, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("[OK] app/routes/technicians.py:155 actualizado con int(user_id) exitosamente.")
