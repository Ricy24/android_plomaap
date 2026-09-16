import json

BACKUP_FILE = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\database_migration\backups\backup_plomapp_20260909_125027.json"

with open(BACKUP_FILE, encoding="utf-8") as fp:
    data = json.load(fp)

users_ids = {r["id"] for r in data["tables"]["users"]["rows"]}
services_ids = {r["id"] for r in data["tables"]["services"]["rows"]}
address_ids = {r["id"] for r in data["tables"]["user_addresses"]["rows"]}

print("Users IDs:", users_ids)
print("Services IDs:", services_ids)
print("Address IDs:", address_ids)

print("\n--- Validando appointments FKs ---")
for apt in data["tables"]["appointments"]["rows"]:
    u_ok = apt["user_id"] in users_ids
    t_ok = apt["technician_id"] is None or apt["technician_id"] in users_ids
    s_ok = apt["service_id"] in services_ids
    a_ok = apt["user_address_id"] is None or apt["user_address_id"] in address_ids
    if not (u_ok and t_ok and s_ok and a_ok):
        print("  Error en apt ID", apt["id"], f"u_ok={u_ok}, t_ok={t_ok}, s_ok={s_ok}, a_ok={a_ok}")
print("Validación de appointments completada.")

print("\n--- Validando legacy.solicitudes FKs ---")
usuarios_ids = {r["usuario_id"] for r in data["tables"]["usuarios"]["rows"]}
for sol in data["tables"]["solicitudes_servicio"]["rows"]:
    c_ok = sol["cliente_id"] is None or sol["cliente_id"] in usuarios_ids
    t_ok = sol["tecnico_id"] is None or sol["tecnico_id"] in usuarios_ids
    if not (c_ok and t_ok):
        print("  Error en sol ID", sol["id"], f"c_ok={c_ok}, t_ok={t_ok}")
print("Validación de legacy solicitudes completada.")
