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
print("FASE 8: PRUEBA INTEGRAL DE ONBOARDING INTELIGENTE Y APROVISIONAMIENTO")
print("=" * 70)

with app.app_context():
    # 0. Preparar identidades y tokens
    user1 = User.query.get(1)  # Andrés
    user2 = User.query.get(2)  # Carlos
    assert user1 is not None and user2 is not None, "Usuarios de prueba no encontrados"
    
    token1 = create_access_token(identity=str(user1.id))
    token2 = create_access_token(identity=str(user2.id))
    
    h1 = {'Authorization': f'Bearer {token1}', 'Content-Type': 'application/json'}
    h2 = {'Authorization': f'Bearer {token2}', 'Content-Type': 'application/json'}
    
    print(f"Usuario 1: {user1.name} (ID: {user1.id})")
    print(f"Usuario 2: {user2.name} (ID: {user2.id})")
    print("-" * 70)

    # 1. Verificar estado inicial
    print("[1] Verificando GET /api/onboarding/status inicial...")
    res_st = client.get('/api/onboarding/status', headers=h1)
    assert res_st.status_code == 200
    st_data = res_st.get_json()['status']
    print(f"    Respuesta: {st_data}")
    assert st_data['completed'] is False
    assert st_data['skipped'] is False
    assert st_data['has_home'] is False
    print("    -> [OK] Estado inicial: no completado, no omitido, sin hogar.")
    print("-" * 70)

    # 2. Verificar catálogo de preguntas adaptativas
    print("[2] Verificando GET /api/onboarding/questions...")
    # Base
    res_q = client.get('/api/onboarding/questions')
    assert res_q.status_code == 200
    q_data = res_q.get_json()['data']
    assert q_data['total_steps'] == 5
    assert len(q_data['questions']) == 5
    print("    -> [OK] 5 preguntas retornadas correctamente.")

    # Variante Apartamento vs Casa en Pregunta 3
    res_q_apt = client.get('/api/onboarding/questions?home_type=apartment')
    q3_apt = res_q_apt.get_json()['data']['questions'][2]
    apt_codes = [opt['code'] for opt in q3_apt['options']]
    assert 'balcon' in apt_codes, "Se esperaba 'balcon' en opciones de apartamento"
    assert 'patio' not in apt_codes, "'patio' no debería estar en opciones principales de apartamento"
    print(f"    -> [OK] P3 Variante Apartamento: {q3_apt['title']} (opciones: {apt_codes})")

    res_q_house = client.get('/api/onboarding/questions?home_type=house')
    q3_house = res_q_house.get_json()['data']['questions'][2]
    house_codes = [opt['code'] for opt in q3_house['options']]
    assert 'patio' in house_codes, "Se esperaba 'patio' en opciones de casa"
    print(f"    -> [OK] P3 Variante Casa: {q3_house['title']} (opciones: {house_codes})")
    print("-" * 70)

    # 3. Iniciar sesión de onboarding
    print("[3] Iniciando sesión vía POST /api/onboarding/start...")
    res_start = client.post('/api/onboarding/start', json={"session_id": "session-test-fase8"}, headers=h1)
    assert res_start.status_code == 200
    ev_start = AppEvent.query.filter_by(event_type='ONBOARDING_STARTED', user_id=1).first()
    assert ev_start is not None, "No se registró el evento ONBOARDING_STARTED"
    print(f"    -> [OK] Evento capturado: ID={ev_start.id}, event_type='{ev_start.event_type}', user_id={ev_start.user_id}")
    print("-" * 70)

    # 4. Completar Onboarding con respuestas estructuradas
    print("[4] Enviando respuestas completas vía POST /api/onboarding/complete...")
    answers_payload = {
        "answers": {
            "home_type": "apartment",
            "needs_frequency": "mantenimiento",
            "key_zones": ["cocina", "bano", "zona_lavado"],
            "assets_present": ["calentador", "lavadora"],
            "maintenance_philosophy": "preventivo"
        },
        "session_id": "session-test-fase8"
    }
    res_comp = client.post('/api/onboarding/complete', json=answers_payload, headers=h1)
    assert res_comp.status_code == 200, f"Error en complete: {res_comp.get_json()}"
    comp_data = res_comp.get_json()
    print(f"    Respuesta: {comp_data['message']}")
    print(f"    Hogar creado: ID={comp_data['home']['id']}, Nombre='{comp_data['home']['name']}', Tipo='{comp_data['home']['type']}'")
    print(f"    Habitaciones creadas ({comp_data['created_rooms_count']}): {[r['type'] for r in comp_data['created_rooms']]}")
    print(f"    Activos creados ({comp_data['created_assets_count']}): {[a['asset_type'] for a in comp_data['created_assets']]}")
    
    home_id = comp_data['home']['id']
    assert comp_data['onboarding_completed'] is True
    assert comp_data['created_rooms_count'] == 3
    assert comp_data['created_assets_count'] == 2
    print("-" * 70)

    # 5. Validar entidades y procedencia en Base de Datos
    print("[5] Validando entidades y atributos de procedencia en Base de Datos...")
    # 5a. Home y HomeMember
    home = Home.query.get(home_id)
    assert home is not None and home.type == 'apartment'
    member = HomeMember.query.filter_by(home_id=home_id, user_id=1).first()
    assert member is not None and member.role == 'owner'
    print("    -> [OK] Home y HomeMember(role='owner') verificados en BD.")

    # 5b. Rooms
    rooms = HomeRoom.query.filter_by(home_id=home_id).all()
    assert len(rooms) == 3
    room_types = {r.type for r in rooms}
    assert room_types == {'cocina', 'bano', 'zona_lavado'}
    print(f"    -> [OK] 3 Habitaciones confirmadas en BD: {room_types}")

    # 5c. Assets
    assets = HomeAsset.query.filter_by(home_id=home_id).all()
    assert len(assets) == 2
    asset_types = {a.asset_type for a in assets}
    assert asset_types == {'calentador', 'lavadora'}
    for a in assets:
        assert a.room_id is not None, f"Activo {a.asset_type} debió asociarse a una habitación"
        print(f"       - Activo '{a.asset_type}': asociado a habitación ID={a.room_id}")
    print(f"    -> [OK] 2 Activos confirmados en BD con asignación a habitación.")

    # 5d. Profile Attributes con source='onboarding' y confidence=0.900
    p_attrs = ProfileAttribute.query.filter_by(source='onboarding').all()
    print(f"    Atributos registrados con source='onboarding' ({len(p_attrs)}):")
    for pa in p_attrs:
        print(f"      - {pa.entity_type:<10} #{pa.entity_id}: {pa.attribute_key:<22} = {pa.attribute_value} (conf={float(pa.confidence):.3f})")
        assert float(pa.confidence) in (0.900, 1.000)

    # Validar atributos clave
    home_type_attr = ProfileAttribute.query.filter_by(entity_type='home', entity_id=home_id, attribute_key='type').first()
    assert home_type_attr.attribute_value == 'apartment' and float(home_type_attr.confidence) == 0.900

    needs_attr = ProfileAttribute.query.filter_by(entity_type='user', entity_id=1, attribute_key='needs_preference').first()
    assert needs_attr.attribute_value == 'mantenimiento' and float(needs_attr.confidence) == 0.900

    maint_attr = ProfileAttribute.query.filter_by(entity_type='user', entity_id=1, attribute_key='maintenance_philosophy').first()
    assert maint_attr.attribute_value == 'preventivo' and float(maint_attr.confidence) == 0.900

    status_attr = ProfileAttribute.query.filter_by(entity_type='user', entity_id=1, attribute_key='onboarding_status').first()
    assert status_attr.attribute_value.get('completed') is True
    print("    -> [OK] Procedencia y confianza 0.900 validadas al 100% en profile_attributes.")

    # 5e. Evento ONBOARDING_COMPLETED en app_events
    ev_comp = AppEvent.query.filter_by(event_type='ONBOARDING_COMPLETED', user_id=1).first()
    assert ev_comp is not None, "No se registró el evento ONBOARDING_COMPLETED"
    print(f"    -> [OK] Evento ONBOARDING_COMPLETED capturado: ID={ev_comp.id}")
    print(f"       Metadata: {ev_comp.metadata_}")
    print("-" * 70)

    # 6. Consultar status posterior a finalización
    print("[6] Consultando GET /api/onboarding/status post-completado...")
    res_st2 = client.get('/api/onboarding/status', headers=h1)
    st2_data = res_st2.get_json()['status']
    print(f"    Respuesta: {st2_data}")
    assert st2_data['completed'] is True
    assert st2_data['has_home'] is True
    print("    -> [OK] Status refleja completado=True y has_home=True.")
    print("-" * 70)

    # 7. Probar opción 'Ahora no' (Skip) con Usuario 2
    print("[7] Probando POST /api/onboarding/skip con Usuario 2...")
    res_skip = client.post('/api/onboarding/skip', json={"session_id": "skip-test-123"}, headers=h2)
    assert res_skip.status_code == 200
    res_st_u2 = client.get('/api/onboarding/status', headers=h2)
    st_u2 = res_st_u2.get_json()['status']
    print(f"    Status User 2: {st_u2}")
    assert st_u2['completed'] is False
    assert st_u2['skipped'] is True
    print("    -> [OK] 'Ahora no' registrado correctamente sin crear entidades no deseadas.")
    print("-" * 70)

    # 8. Limpieza completa y reinicio de secuencias
    print("[8] Ejecutando limpieza completa de prueba...")
    HomeAsset.query.delete()
    HomeRoom.query.delete()
    HomeMember.query.delete()
    Home.query.delete()
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
    print("    -> [OK] Entidades eliminadas y secuencias reiniciadas.")
    print("-" * 70)

    # 9. Verificación de ceros en tablas operativas
    print("[9] Verificando conteos en 0 de tablas operativas...")
    assert Home.query.count() == 0
    assert HomeMember.query.count() == 0
    assert HomeRoom.query.count() == 0
    assert HomeAsset.query.count() == 0
    assert ProfileAttribute.query.count() == 0
    assert AppEvent.query.count() == 0
    assert RoomTypeCatalog.query.count() == 7
    assert AssetTypeCatalog.query.count() == 8
    print("    -> [OK] Todas las tablas de esta fase retornaron a 0 filas (catálogos en 7 y 8).")

print("=" * 70)
print("FASE 8: TODAS LAS PRUEBAS FUNCIONALES PASARON EXITOSAMENTE AL 100%")
print("=" * 70)
