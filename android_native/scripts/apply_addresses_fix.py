import os
import shutil

TARGET = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\addresses.py"
BACKUP = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\addresses.py.bak"

# 1. Create backup
shutil.copy2(TARGET, BACKUP)
print(f"[OK] Backup creado en {BACKUP}")

with open(TARGET, 'r', encoding='utf-8') as f:
    content = f.read()

target_block = """    # If user has no saved addresses, but has a profile address, auto-create one as 'Casa' default
    if not addresses:
        user = User.query.get(user_id)
        if user and user.address and user.address.strip():
            auto_addr = UserAddress(
                user_id=user.id,
                label='Casa',
                address=user.address.strip(),
                is_default=True
            )
            db.session.add(auto_addr)
            db.session.commit()
            addresses = [auto_addr]"""

assert target_block in content, "ERROR: Target block not found in addresses.py"

new_content = content.replace(target_block + "\n\n", "").replace(target_block + "\n", "")

with open(TARGET, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("[OK] Bloque de auto-creación eliminado exitosamente de addresses.py.")
