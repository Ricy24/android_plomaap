import os
import sys
import psycopg2
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
sys.path.insert(0, BACKEND_DIR)
os.chdir(BACKEND_DIR)
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)

from app import create_app
from app.database.extensions import db
from app.database.models import (
    User, Home, HomeMember, HomeRoom, HomeAsset,
    ProfileAttribute, AppEvent
)
from flask_jwt_extended import create_access_token

app = create_app('development')
client = app.test_client()

print("=" * 70)
print("TEST EXTENDIDO: PATCH + ROLES MEMBER Y ADMIN")
print("=" * 70)

with app.app_context():
    # Setup users
    user_owner = User.query.get(1)    # Owner
    user_admin = User.query.get(2)    # Admin member
    user_member = User.query.get(3)   # Standard member (read-only)
    
    token_owner = create_access_token(identity=str(user_owner.id))
    token_admin = create_access_token(identity=str(user_admin.id))
    token_member = create_access_token(identity=str(user_member.id))
    
    h_owner = {'Authorization': f'Bearer {token_owner}', 'Content-Type': 'application/json'}
    h_admin = {'Authorization': f'Bearer {token_admin}', 'Content-Type': 'application/json'}
    h_member = {'Authorization': f'Bearer {token_member}', 'Content-Type': 'application/json'}
    
    # 1. Create Home as Owner
    res = client.post('/api/homes', json={"name": "Hogar Roles Test", "type": "house", "rooms_count": 2}, headers=h_owner)
    assert res.status_code == 201
    home_id = res.get_json()['home']['id']
    print(f"[OK] Hogar creado con ID={home_id}")
    
    # Add user 2 as admin and user 3 as member
    m_admin = HomeMember(home_id=home_id, user_id=user_admin.id, role='admin')
    m_member = HomeMember(home_id=home_id, user_id=user_member.id, role='member')
    db.session.add(m_admin)
    db.session.add(m_member)
    db.session.commit()
    print("[OK] Miembros asignados: User 2 como 'admin', User 3 como 'member'")
    
    # 2. Test Member permissions (Read OK, Write Forbidden)
    print("\n--- Verificando rol 'member' (User 3) ---")
    # Read OK
    assert client.get(f'/api/homes/{home_id}', headers=h_member).status_code == 200
    assert client.get(f'/api/homes/{home_id}/rooms', headers=h_member).status_code == 200
    assert client.get(f'/api/homes/{home_id}/assets', headers=h_member).status_code == 200
    print("  [OK] GET home, rooms, assets -> 200 OK para 'member'")
    
    # Write Blocked (403)
    assert client.patch(f'/api/homes/{home_id}', json={"name": "X"}, headers=h_member).status_code == 403
    assert client.post(f'/api/homes/{home_id}/rooms', json={"name": "R", "type": "sala"}, headers=h_member).status_code == 403
    assert client.post(f'/api/homes/{home_id}/assets', json={"asset_type": "lavadora"}, headers=h_member).status_code == 403
    assert client.delete(f'/api/homes/{home_id}', headers=h_member).status_code == 403
    print("  [OK] PATCH home, POST room, POST asset, DELETE home -> 403 Forbidden para 'member'")
    
    # 3. Test Admin permissions (Write OK, Delete Home Forbidden)
    print("\n--- Verificando rol 'admin' (User 2) ---")
    # PATCH home
    res_patch = client.patch(f'/api/homes/{home_id}', json={"name": "Hogar Actualizado por Admin", "rooms_count": 5}, headers=h_admin)
    assert res_patch.status_code == 200
    print("  [OK] PATCH home por 'admin' -> 200 OK")
    
    # Check provenance updated
    p_rooms = ProfileAttribute.query.filter_by(entity_type='home', entity_id=home_id, attribute_key='rooms_count').first()
    assert p_rooms is not None and p_rooms.attribute_value == 5
    print("  [OK] Provenance de rooms_count actualizada a 5 por 'admin'")
    
    # POST room by admin
    res_room = client.post(f'/api/homes/{home_id}/rooms', json={"name": "Sala Principal", "type": "sala"}, headers=h_admin)
    assert res_room.status_code == 201
    room_id = res_room.get_json()['room']['id']
    print(f"  [OK] POST room por 'admin' -> 201 Created (room_id={room_id})")
    
    # PATCH room by admin
    res_patch_room = client.patch(f'/api/homes/{home_id}/rooms/{room_id}', json={"name": "Sala de Estar", "type": "habitacion"}, headers=h_admin)
    assert res_patch_room.status_code == 200
    p_room_type = ProfileAttribute.query.filter_by(entity_type='home_room', entity_id=room_id, attribute_key='type').first()
    assert p_room_type.attribute_value == 'habitacion'
    print("  [OK] PATCH room por 'admin' -> 200 OK, provenance actualizada a 'habitacion'")
    
    # POST asset by admin
    res_asset = client.post(f'/api/homes/{home_id}/assets', json={"asset_type": "lavadora", "brand": "LG", "model": "TurboWash"}, headers=h_admin)
    assert res_asset.status_code == 201
    asset_id = res_asset.get_json()['asset']['id']
    print(f"  [OK] POST asset por 'admin' -> 201 Created (asset_id={asset_id})")
    
    # PATCH asset by admin
    res_patch_asset = client.patch(f'/api/homes/{home_id}/assets/{asset_id}', json={"brand": "Samsung", "model": "EcoBubble"}, headers=h_admin)
    assert res_patch_asset.status_code == 200
    p_brand = ProfileAttribute.query.filter_by(entity_type='home_asset', entity_id=asset_id, attribute_key='brand').first()
    assert p_brand.attribute_value == 'Samsung'
    print("  [OK] PATCH asset por 'admin' -> 200 OK, provenance actualizada a 'Samsung'")
    
    # Admin cannot DELETE home (only owner can delete home)
    assert client.delete(f'/api/homes/{home_id}', headers=h_admin).status_code == 403
    print("  [OK] DELETE home por 'admin' -> 403 Forbidden (solo owner puede eliminar hogar)")
    
    # Admin CAN delete asset and room
    assert client.delete(f'/api/homes/{home_id}/assets/{asset_id}', headers=h_admin).status_code == 200
    print(f"  [OK] DELETE asset #{asset_id} por 'admin' -> 200 OK")
    assert client.delete(f'/api/homes/{home_id}/rooms/{room_id}', headers=h_admin).status_code == 200
    print(f"  [OK] DELETE room #{room_id} por 'admin' -> 200 OK")
    
    # 4. Owner deletes home
    print("\n--- Eliminación final por Owner ---")
    assert client.delete(f'/api/homes/{home_id}', headers=h_owner).status_code == 200
    print("  [OK] DELETE home por Owner -> 200 OK")
    
    # Cleanup attributes and events
    ProfileAttribute.query.delete()
    AppEvent.query.delete()
    db.session.commit()
    db.session.execute(db.text("ALTER SEQUENCE profile_attributes_id_seq RESTART WITH 1;"))
    db.session.execute(db.text("ALTER SEQUENCE app_events_id_seq RESTART WITH 1;"))
    db.session.execute(db.text("ALTER SEQUENCE homes_id_seq RESTART WITH 1;"))
    db.session.execute(db.text("ALTER SEQUENCE home_members_id_seq RESTART WITH 1;"))
    db.session.execute(db.text("ALTER SEQUENCE home_rooms_id_seq RESTART WITH 1;"))
    db.session.execute(db.text("ALTER SEQUENCE home_assets_id_seq RESTART WITH 1;"))
    db.session.commit()
    print("  [OK] Limpieza y reinicio de secuencias completados.")
    
    # Final check
    assert Home.query.count() == 0
    assert HomeMember.query.count() == 0
    assert HomeRoom.query.count() == 0
    assert HomeAsset.query.count() == 0
    assert ProfileAttribute.query.count() == 0
    assert AppEvent.query.count() == 0

print("=" * 70)
print("TEST EXTENDIDO COMPLETADO AL 100% EXITOSAMENTE")
print("=" * 70)
