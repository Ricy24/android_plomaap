import os
import sys

print("=" * 65)
print("EVALUACIÓN FORMAL DE LAS CONDICIONES BOOLEANAS (PYTHON)")
print("=" * 65)

# Variables reales
user1_id_jwt = '1'        # string devuelto por get_jwt_identity()
user14_id_jwt = '14'      # string devuelto por get_jwt_identity()
tech2_id_jwt = '2'        # string devuelto por get_jwt_identity()
tech3_id_jwt = '3'        # string devuelto por get_jwt_identity()

apt1_user_id = 1          # int en base de datos
apt1_tech_id = 3          # int en base de datos

apt10_user_id = 14        # int en base de datos
apt10_tech_id = 2         # int en base de datos

print("\n1. Escenario 1: Dueño legítimo (user 1) accede a cita 1 (apt1.user_id = 1)")
old_cond = (apt1_user_id != user1_id_jwt)       # 1 != '1'
new_cond = (apt1_user_id != int(user1_id_jwt))  # 1 != 1
print(f"   CÓDIGO ANTERIOR: apt.user_id != user_id -> {apt1_user_id} != '{user1_id_jwt}' es {old_cond} -> Bloquea con 403")
print(f"   CÓDIGO ACTUAL:   apt.user_id != int(user_id) -> {apt1_user_id} != {int(user1_id_jwt)} es {new_cond} -> Permite con 200")

print("\n2. Escenario 2: Usuario ajeno (user 1) accede a cita 10 (apt10.user_id = 14)")
old_cond = (apt10_user_id != user1_id_jwt)       # 14 != '1'
new_cond = (apt10_user_id != int(user1_id_jwt))  # 14 != 1
print(f"   CÓDIGO ANTERIOR: apt.user_id != user_id -> {apt10_user_id} != '{user1_id_jwt}' es {old_cond} -> Bloquea con 403")
print(f"   CÓDIGO ACTUAL:   apt.user_id != int(user_id) -> {apt10_user_id} != {int(user1_id_jwt)} es {new_cond} -> Bloquea con 403")

print("\n3. Escenario 3: Técnico asignado (tech 2) accede a cita 10 (apt10.technician_id = 2)")
old_cond = (apt10_tech_id != tech2_id_jwt)       # 2 != '2'
new_cond = (apt10_tech_id != int(tech2_id_jwt))  # 2 != 2
print(f"   CÓDIGO ANTERIOR: apt.tech_id != user_id -> {apt10_tech_id} != '{tech2_id_jwt}' es {old_cond} -> Bloquea con 403")
print(f"   CÓDIGO ACTUAL:   apt.tech_id != int(user_id) -> {apt10_tech_id} != {int(tech2_id_jwt)} es {new_cond} -> Permite con 200")

print("\n4. Escenario 4: Usuario ajeno (user 1) actualiza cita 10 (apt10.user_id = 14)")
old_cond = (apt10_user_id != user1_id_jwt)       # 14 != '1'
new_cond = (apt10_user_id != int(user1_id_jwt))  # 14 != 1
print(f"   CÓDIGO ANTERIOR: apt.user_id != user_id -> {apt10_user_id} != '{user1_id_jwt}' es {old_cond} -> Bloquea con 403")
print(f"   CÓDIGO ACTUAL:   apt.user_id != int(user_id) -> {apt10_user_id} != {int(user1_id_jwt)} es {new_cond} -> Bloquea con 403")
