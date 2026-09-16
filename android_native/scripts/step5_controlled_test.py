import os
import sys
import json
import psycopg2
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
sys.path.insert(0, BACKEND_DIR)
os.chdir(BACKEND_DIR)
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)
pw = os.getenv("POSTGRES_PASSWORD", "")

from app import create_app
from app.database.extensions import db
from app.database.models import (
    User, Home, HomeMember, HomeRoom, HomeAsset,
    ProfileAttribute, AppEvent, RoomTypeCatalog, AssetTypeCatalog
)
from flask_jwt_extended import create_access_token

app = create_app('development')
client = app.test_client()

print("=" * 70)
print("FASE 7 - PASO 5: PRUEBA CONTROLADA DE CICLO DE VIDA COMPLETO")
print("=" * 70)

with app.app_context():
    # 0. Preparar identidades y tokens
    user1 = User.query.get(1)
    user2 = User.query.get(2)
    assert user1 is not None, "Usuario 1 (Andrés) no encontrado"
    assert user2 is not None, "Usuario 2 no encontrado"
    
    token1 = create_access_token(identity=str(user1.id))
    token2 = create_access_token(identity=str(user2.id))
    
    headers_u1 = {'Authorization': f'Bearer {token1}', 'Content-Type': 'application/json'}
    headers_u2 = {'Authorization': f'Bearer {token2}', 'Content-Type': 'application/json'}
    
    print(f"Usuario 1 (Propietario): {user1.name} (ID: {user1.id}, email: {user1.email})")
    print(f"Usuario 2 (Ajeno):        {user2.name} (ID: {user2.id}, email: {user2.email})")
    print("-" * 70)

    # 1. Crear hogar de prueba con POST /api/homes
    print("[1] Creando hogar de prueba vía POST /api/homes (User 1)...")
    home_payload = {
        "name": "Apartamento 402 - Torre Norte",
        "type": "apartment",
        "approximate_area": 92.50,
        "rooms_count": 3
    }
    res = client.post('/api/homes', json=home_payload, headers=headers_u1)
    print(f"    Status: {res.status_code}")
    res_data = res.get_json()
    print(f"    Respuesta: {res_data}")
    assert res.status_code == 201, f"Error creando hogar: {res_data}"
    home_id = res_data['home']['id']
    print(f"    -> Hogar creado exitosamente con ID: {home_id}")
    print("-" * 70)

    # 2. Confirmar membresía automática con rol 'owner'
    print("[2] Verificando creación automática en home_members...")
    membership = HomeMember.query.filter_by(home_id=home_id, user_id=user1.id).first()
    assert membership is not None, "No se creó la fila en home_members"
    assert membership.role == 'owner', f"El rol esperado era 'owner', se obtuvo '{membership.role}'"
    print(f"    -> [OK] Membresía confirmada: ID={membership.id}, home_id={membership.home_id}, user_id={membership.user_id}, role='{membership.role}'")
    print("-" * 70)

    # 3. Confirmar registro en profile_attributes
    print("[3] Verificando registro de procedencia en profile_attributes...")
    attrs = ProfileAttribute.query.filter_by(entity_type='home', entity_id=home_id).all()
    print(f"    Filas de atributos encontradas ({len(attrs)}):")
    attr_dict = {}
    for a in attrs:
        attr_dict[a.attribute_key] = a
        print(f"      - {a.attribute_key:<20}: valor={a.attribute_value} (source='{a.source}', confidence={float(a.confidence):.3f}, updated_at={a.updated_at})")
    
    assert 'type' in attr_dict, "Atributo 'type' no registrado en profile_attributes"
    assert attr_dict['type'].attribute_value == 'apartment'
    assert attr_dict['type'].source == 'explicit_user_input'
    assert float(attr_dict['type'].confidence) == 1.000

    assert 'approximate_area' in attr_dict, "Atributo 'approximate_area' no registrado"
    assert float(attr_dict['approximate_area'].attribute_value) == 92.50
    assert attr_dict['approximate_area'].source == 'explicit_user_input'
    assert float(attr_dict['approximate_area'].confidence) == 1.000

    assert 'rooms_count' in attr_dict, "Atributo 'rooms_count' no registrado"
    assert attr_dict['rooms_count'].attribute_value == 3
    assert attr_dict['rooms_count'].source == 'explicit_user_input'
    assert float(attr_dict['rooms_count'].confidence) == 1.000
    print("    -> [OK] Procedencia de los 3 atributos del hogar validada al 100%.")
    print("-" * 70)

    # 4. Crear habitación de prueba
    print("[4] Creando habitación de prueba vía POST /api/homes/<home_id>/rooms...")
    # Validación negativa de catálogo primero
    bad_room = client.post(f'/api/homes/{home_id}/rooms', json={"name": "Habitación Falsa", "type": "sala_de_cine_inexistente"}, headers=headers_u1)
    assert bad_room.status_code == 400, f"Debería fallar validación de catálogo, status={bad_room.status_code}"
    print(f"    -> [OK] Validación negativa de catálogo de habitación: 400 Bad Request ({bad_room.get_json()['error']})")
    
    room_payload = {
        "name": "Cocina Principal",
        "type": "cocina"
    }
    res_room = client.post(f'/api/homes/{home_id}/rooms', json=room_payload, headers=headers_u1)
    print(f"    Status: {res_room.status_code}")
    room_data = res_room.get_json()
    print(f"    Respuesta: {room_data}")
    assert res_room.status_code == 201, f"Error creando habitación: {room_data}"
    room_id = room_data['room']['id']
    print(f"    -> Habitación creada con ID: {room_id}")
    
    # Confirmar provenance de habitación
    room_attrs = ProfileAttribute.query.filter_by(entity_type='home_room', entity_id=room_id).all()
    assert len(room_attrs) == 1, f"Se esperaba 1 atributo para la habitación, hay {len(room_attrs)}"
    assert room_attrs[0].attribute_key == 'type'
    assert room_attrs[0].attribute_value == 'cocina'
    assert room_attrs[0].source == 'explicit_user_input'
    assert float(room_attrs[0].confidence) == 1.000
    print(f"    -> [OK] Provenance de habitación confirmada: key='{room_attrs[0].attribute_key}', valor='{room_attrs[0].attribute_value}', source='{room_attrs[0].source}', confidence={float(room_attrs[0].confidence):.3f}")
    print("-" * 70)

    # 5. Crear activo de prueba
    print("[5] Creando activo de prueba vía POST /api/homes/<home_id>/assets...")
    # Validación negativa de catálogo primero
    bad_asset = client.post(f'/api/homes/{home_id}/assets', json={"asset_type": "computador_cuantico"}, headers=headers_u1)
    assert bad_asset.status_code == 400, f"Debería fallar validación de activo, status={bad_asset.status_code}"
    print(f"    -> [OK] Validación negativa de catálogo de activos: 400 Bad Request ({bad_asset.get_json()['error']})")

    asset_payload = {
        "room_id": room_id,
        "asset_type": "calentador",
        "brand": "Bosch",
        "model": "Therm 4000 10L",
        "installation_date": "2024-05-15",
        "last_maintenance": "2025-02-10",
        "warranty_until": "2026-05-15",
        "metadata": {"capacity_liters": 10, "gas_type": "gas_natural"}
    }
    res_asset = client.post(f'/api/homes/{home_id}/assets', json=asset_payload, headers=headers_u1)
    print(f"    Status: {res_asset.status_code}")
    asset_data = res_asset.get_json()
    print(f"    Respuesta: {asset_data}")
    assert res_asset.status_code == 201, f"Error creando activo: {asset_data}"
    asset_id = asset_data['asset']['id']
    print(f"    -> Activo creado con ID: {asset_id}")

    # Confirmar provenance de activo (asset_type, brand, model)
    asset_attrs = ProfileAttribute.query.filter_by(entity_type='home_asset', entity_id=asset_id).all()
    print(f"    Atributos registrados para activo #{asset_id} ({len(asset_attrs)}):")
    asset_attr_dict = {}
    for a in asset_attrs:
        asset_attr_dict[a.attribute_key] = a
        print(f"      - {a.attribute_key:<15}: valor='{a.attribute_value}', source='{a.source}', confidence={float(a.confidence):.3f}")
    assert 'asset_type' in asset_attr_dict and asset_attr_dict['asset_type'].attribute_value == 'calentador'
    assert 'brand' in asset_attr_dict and asset_attr_dict['brand'].attribute_value == 'Bosch'
    assert 'model' in asset_attr_dict and asset_attr_dict['model'].attribute_value == 'Therm 4000 10L'
    print("    -> [OK] Provenance de activo validada al 100%.")
    print("-" * 70)

    # 6. Confirmar evento ASSET_CREATED en app_events
    print("[6] Verificando evento ASSET_CREATED en app_events...")
    event = AppEvent.query.filter_by(
        event_type='ASSET_CREATED',
        entity_type='home_asset',
        entity_id=asset_id
    ).first()
    assert event is not None, f"No se encontró el evento ASSET_CREATED para entity_id={asset_id}"
    assert event.user_id == user1.id, f"user_id incorrecto en evento: {event.user_id}"
    print(f"    -> [OK] Evento capturado en app_events: ID={event.id}, event_type='{event.event_type}', user_id={event.user_id}, entity_type='{event.entity_type}', entity_id={event.entity_id}")
    print(f"       Metadata del evento: {event.metadata_}")
    print("-" * 70)

    # 7. Pruebas de autorización negativa (User 2)
    print("[7] Ejecutando pruebas de autorización negativa con Usuario 2 (ajeno)...")
    
    # 7a. GET /api/homes/<id> ajeno
    r_get = client.get(f'/api/homes/{home_id}', headers=headers_u2)
    print(f"    - GET /api/homes/{home_id} -> Status {r_get.status_code} (Esperado: 403)")
    assert r_get.status_code == 403, f"Esperado 403, obtenido {r_get.status_code}"

    # 7b. PATCH /api/homes/<id> ajeno
    r_patch = client.patch(f'/api/homes/{home_id}', json={"name": "Hogar Hackeado"}, headers=headers_u2)
    print(f"    - PATCH /api/homes/{home_id} -> Status {r_patch.status_code} (Esperado: 403)")
    assert r_patch.status_code == 403, f"Esperado 403, obtenido {r_patch.status_code}"

    # 7c. POST /api/homes/<id>/rooms ajeno
    r_post_room = client.post(f'/api/homes/{home_id}/rooms', json={"name": "Invasión", "type": "bano"}, headers=headers_u2)
    print(f"    - POST /api/homes/{home_id}/rooms -> Status {r_post_room.status_code} (Esperado: 403)")
    assert r_post_room.status_code == 403, f"Esperado 403, obtenido {r_post_room.status_code}"

    # 7d. GET /api/homes/<id>/rooms ajeno
    r_get_rooms = client.get(f'/api/homes/{home_id}/rooms', headers=headers_u2)
    print(f"    - GET /api/homes/{home_id}/rooms -> Status {r_get_rooms.status_code} (Esperado: 403)")
    assert r_get_rooms.status_code == 403, f"Esperado 403, obtenido {r_get_rooms.status_code}"

    # 7e. POST /api/homes/<id>/assets ajeno
    r_post_asset = client.post(f'/api/homes/{home_id}/assets', json={"asset_type": "lavadora"}, headers=headers_u2)
    print(f"    - POST /api/homes/{home_id}/assets -> Status {r_post_asset.status_code} (Esperado: 403)")
    assert r_post_asset.status_code == 403, f"Esperado 403, obtenido {r_post_asset.status_code}"

    # 7f. GET /api/homes/<id>/assets ajeno
    r_get_assets = client.get(f'/api/homes/{home_id}/assets', headers=headers_u2)
    print(f"    - GET /api/homes/{home_id}/assets -> Status {r_get_assets.status_code} (Esperado: 403)")
    assert r_get_assets.status_code == 403, f"Esperado 403, obtenido {r_get_assets.status_code}"

    # 7g. DELETE /api/homes/<id> ajeno
    r_del = client.delete(f'/api/homes/{home_id}', headers=headers_u2)
    print(f"    - DELETE /api/homes/{home_id} -> Status {r_del.status_code} (Esperado: 403)")
    assert r_del.status_code == 403, f"Esperado 403, obtenido {r_del.status_code}"

    # 7h. GET /api/homes lista de User 2 no incluye el hogar de User 1
    r_list_u2 = client.get('/api/homes', headers=headers_u2)
    u2_homes = r_list_u2.get_json().get('homes', [])
    assert not any(h['id'] == home_id for h in u2_homes), "El hogar ajeno no debe aparecer en listado de User 2"
    print(f"    - GET /api/homes de User 2 -> Retorna {len(u2_homes)} hogares (ninguno es #{home_id})")
    print("    -> [OK] Todas las 8 verificaciones de autorización negativa pasaron exitosamente (403/aislamiento estricto).")
    print("-" * 70)

    # 8. Eliminación del hogar de prueba y verificación de cascada
    print("[8] Eliminando hogar de prueba vía DELETE /api/homes/<id> (User 1)...")
    res_del = client.delete(f'/api/homes/{home_id}', headers=headers_u1)
    print(f"    Status: {res_del.status_code}")
    assert res_del.status_code == 200, f"Error eliminando hogar: {res_del.get_json()}"

    # Verificar borrado en cascada
    assert Home.query.get(home_id) is None, "El hogar aún existe en DB"
    assert HomeMember.query.filter_by(home_id=home_id).count() == 0, "Quedaron miembros huérfanos"
    assert HomeRoom.query.filter_by(home_id=home_id).count() == 0, "Quedaron habitaciones huérfanas"
    assert HomeAsset.query.filter_by(home_id=home_id).count() == 0, "Quedaron activos huérfanos"
    print("    -> [OK] Cascada verificada: hogar, miembros, habitaciones y activos eliminados por FK CASCADE.")

    # Limpieza manual de profile_attributes y app_events del test
    print("    Limpiando manualmente profile_attributes y app_events de la prueba...")
    deleted_attrs = ProfileAttribute.query.filter(
        ((ProfileAttribute.entity_type == 'home') & (ProfileAttribute.entity_id == home_id)) |
        ((ProfileAttribute.entity_type == 'home_room') & (ProfileAttribute.entity_id == room_id)) |
        ((ProfileAttribute.entity_type == 'home_asset') & (ProfileAttribute.entity_id == asset_id))
    ).delete()
    deleted_events = AppEvent.query.filter_by(
        event_type='ASSET_CREATED',
        entity_type='home_asset',
        entity_id=asset_id
    ).delete()
    db.session.commit()
    print(f"    -> [OK] Limpiados {deleted_attrs} atributos y {deleted_events} eventos.")
    
    # Reiniciar secuencias para dejar estado prístino
    db.session.execute(db.text("ALTER SEQUENCE profile_attributes_id_seq RESTART WITH 1;"))
    db.session.execute(db.text("ALTER SEQUENCE app_events_id_seq RESTART WITH 1;"))
    db.session.execute(db.text("ALTER SEQUENCE homes_id_seq RESTART WITH 1;"))
    db.session.execute(db.text("ALTER SEQUENCE home_members_id_seq RESTART WITH 1;"))
    db.session.execute(db.text("ALTER SEQUENCE home_rooms_id_seq RESTART WITH 1;"))
    db.session.execute(db.text("ALTER SEQUENCE home_assets_id_seq RESTART WITH 1;"))
    db.session.commit()
    print("-" * 70)

    # 9. Confirmar conteos finales en cero para entidades de esta fase
    print("[9] Verificando que todas las tablas de esta fase regresaron a 0 filas...")
    counts = {
        'homes': Home.query.count(),
        'home_members': HomeMember.query.count(),
        'home_rooms': HomeRoom.query.count(),
        'home_assets': HomeAsset.query.count(),
        'profile_attributes': ProfileAttribute.query.count(),
        'app_events': AppEvent.query.count(),
        'room_type_catalog': RoomTypeCatalog.query.count(),
        'asset_type_catalog': AssetTypeCatalog.query.count()
    }
    for tbl, cnt in counts.items():
        print(f"    - {tbl:<25}: {cnt} filas")
    
    assert counts['homes'] == 0, f"homes tiene {counts['homes']} filas"
    assert counts['home_members'] == 0, f"home_members tiene {counts['home_members']} filas"
    assert counts['home_rooms'] == 0, f"home_rooms tiene {counts['home_rooms']} filas"
    assert counts['home_assets'] == 0, f"home_assets tiene {counts['home_assets']} filas"
    assert counts['profile_attributes'] == 0, f"profile_attributes tiene {counts['profile_attributes']} filas"
    assert counts['app_events'] == 0, f"app_events tiene {counts['app_events']} filas"
    assert counts['room_type_catalog'] == 7, f"room_type_catalog modificado: {counts['room_type_catalog']}"
    assert counts['asset_type_catalog'] == 8, f"asset_type_catalog modificado: {counts['asset_type_catalog']}"

print("=" * 70)
print("PASO 5: CICLO DE VIDA COMPLETO Y LIMPIEZA VALIDADOS AL 100%")
print("=" * 70)
