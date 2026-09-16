import os
import sys

TARGET_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\homes.py"

content = '''"""
Digital Home Routes for PlomApp
FASE 7: Perfil de Hogar (Infraestructura de Fuente/Confianza + CRUD basico)
Endpoints:
  Homes:
    POST   /api/homes
    GET    /api/homes
    GET    /api/homes/<id>
    PATCH  /api/homes/<id>
    DELETE /api/homes/<id>
  Rooms:
    POST   /api/homes/<home_id>/rooms
    GET    /api/homes/<home_id>/rooms
    PATCH  /api/homes/<home_id>/rooms/<room_id>
    DELETE /api/homes/<home_id>/rooms/<room_id>
  Assets:
    POST   /api/homes/<home_id>/assets
    GET    /api/homes/<home_id>/assets
    PATCH  /api/homes/<home_id>/assets/<asset_id>
    DELETE /api/homes/<home_id>/assets/<asset_id>
"""
import logging
from datetime import datetime
from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity

from app.database.extensions import db
from app.database.models import (
    Home, HomeMember, HomeRoom, HomeAsset,
    RoomTypeCatalog, AssetTypeCatalog
)
from app.services.profile_service import record_attribute
from app.services.event_service import track_event

logger = logging.getLogger(__name__)

homes_bp = Blueprint('homes', __name__, url_prefix='/api/homes')

# Mapeo y validacion de tipos de hogar
VALID_HOME_TYPES = {
    'apartment': 'apartment',
    'apartamento': 'apartment',
    'house': 'house',
    'casa': 'house',
    'studio': 'studio',
    'estudio': 'studio',
    'office': 'office',
    'oficina': 'office',
    'other': 'other',
    'otro': 'other'
}


def _get_membership(home_id, user_id):
    """
    Obtiene la autorizacion y rol del usuario para un hogar dado.
    Garantiza comparaciones enteras int(user_id) vs columnas enteras.
    Retorna (has_access, role, home)
    role: 'owner', 'admin', 'member', 'guest' o None
    """
    home = db.session.get(Home, home_id)
    if not home:
        return False, None, None
    
    if home.owner_id == user_id:
        return True, 'owner', home
        
    member = HomeMember.query.filter_by(home_id=home_id, user_id=user_id).first()
    if member:
        return True, member.role, home
        
    return False, None, home


# =====================================================================
# 1. HOMES ENDPOINTS
# =====================================================================

@homes_bp.route('', methods=['POST'])
@jwt_required()
def create_home():
    """
    POST /api/homes
    Crea un nuevo hogar asignando owner_id al usuario autenticado.
    Auto-crea fila en home_members con role='owner'.
    Registra en profile_attributes cada campo provisto.
    """
    user_id = int(get_jwt_identity())
    data = request.get_json() or {}
    
    name = data.get('name')
    if not name or not str(name).strip():
        return jsonify({'error': 'El campo name es obligatorio'}), 400
    name = str(name).strip()
    
    raw_type = data.get('type', 'apartment')
    home_type = VALID_HOME_TYPES.get(str(raw_type).lower())
    if not home_type:
        return jsonify({'error': f"Tipo de hogar invalido: '{raw_type}'. Debe ser: apartment, house, studio, office, other"}), 400
        
    approximate_area = data.get('approximate_area')
    if approximate_area is not None:
        try:
            approximate_area = float(approximate_area)
            if approximate_area < 0:
                return jsonify({'error': 'approximate_area debe ser un numero positivo'}), 400
        except (ValueError, TypeError):
            return jsonify({'error': 'approximate_area debe ser numerico'}), 400
            
    rooms_count = data.get('rooms_count', 0)
    if rooms_count is not None:
        try:
            rooms_count = int(rooms_count)
            if rooms_count < 0:
                return jsonify({'error': 'rooms_count debe ser un entero no negativo'}), 400
        except (ValueError, TypeError):
            return jsonify({'error': 'rooms_count debe ser un entero'}), 400

    try:
        home = Home(
            owner_id=user_id,
            name=name,
            type=home_type,
            approximate_area=approximate_area,
            rooms_count=rooms_count,
            created_at=datetime.utcnow(),
            updated_at=datetime.utcnow()
        )
        db.session.add(home)
        db.session.flush()  # Obtener home.id
        
        # (a) Auto-crear membresia como owner
        member = HomeMember(
            home_id=home.id,
            user_id=user_id,
            role='owner',
            created_at=datetime.utcnow()
        )
        db.session.add(member)
        
        # (b) Registrar procedencia en profile_attributes (commit=False dentro de la transaccion)
        if 'type' in data and data['type'] is not None:
            record_attribute('home', home.id, 'type', home.type, source='explicit_user_input', confidence=1.000, commit=False)
        if 'approximate_area' in data and data['approximate_area'] is not None:
            record_attribute('home', home.id, 'approximate_area', float(home.approximate_area), source='explicit_user_input', confidence=1.000, commit=False)
        if 'rooms_count' in data and data['rooms_count'] is not None:
            record_attribute('home', home.id, 'rooms_count', home.rooms_count, source='explicit_user_input', confidence=1.000, commit=False)
            
        db.session.commit()

        return jsonify({
            'success': True,
            'message': 'Hogar creado exitosamente',
            'home': home.to_dict(enriched=True)
        }), 201

    except Exception as e:
        db.session.rollback()
        logger.error(f"Error creando hogar: {e}", exc_info=True)
        return jsonify({'error': 'Error interno al crear el hogar'}), 500


@homes_bp.route('', methods=['GET'])
@jwt_required()
def list_homes():
    """
    GET /api/homes
    Lista los hogares donde el usuario autenticado es owner o member.
    """
    user_id = int(get_jwt_identity())
    
    # Hogares donde el usuario es dueno o miembro registrado
    member_home_ids = db.select(HomeMember.home_id).filter_by(user_id=user_id)
    homes = Home.query.filter(
        (Home.owner_id == user_id) | (Home.id.in_(member_home_ids))
    ).order_by(Home.id.asc()).all()
    
    return jsonify({
        'success': True,
        'count': len(homes),
        'homes': [h.to_dict(enriched=True) for h in homes]
    }), 200


@homes_bp.route('/<int:id>', methods=['GET'])
@jwt_required()
def get_home(id):
    """
    GET /api/homes/<id>
    Detalle de un hogar. Solo si el usuario es owner o member.
    """
    user_id = int(get_jwt_identity())
    has_access, role, home = _get_membership(id, user_id)
    
    if not home:
        return jsonify({'error': f'Hogar #{id} no encontrado'}), 404
    if not has_access:
        return jsonify({'error': 'No tienes autorizacion para acceder a este hogar'}), 403
        
    data = home.to_dict(enriched=True)
    data['current_user_role'] = role
    data['members'] = [m.to_dict() for m in home.members]
    data['rooms'] = [r.to_dict() for r in home.rooms]
    return jsonify({
        'success': True,
        'home': data
    }), 200


@homes_bp.route('/<int:id>', methods=['PATCH'])
@jwt_required()
def update_home(id):
    """
    PATCH /api/homes/<id>
    Actualiza datos del hogar. Solo owner o admin.
    Registra provenance de los campos cambiados.
    """
    user_id = int(get_jwt_identity())
    has_access, role, home = _get_membership(id, user_id)
    
    if not home:
        return jsonify({'error': f'Hogar #{id} no encontrado'}), 404
    if not has_access or role not in ('owner', 'admin'):
        return jsonify({'error': 'Se requieren privilegios de propietario o administrador para modificar el hogar'}), 403
        
    data = request.get_json() or {}
    updated_fields = []
    
    if 'name' in data:
        new_name = str(data['name']).strip()
        if not new_name:
            return jsonify({'error': 'El nombre no puede estar vacio'}), 400
        home.name = new_name
        updated_fields.append('name')
        
    if 'type' in data:
        raw_type = data['type']
        home_type = VALID_HOME_TYPES.get(str(raw_type).lower())
        if not home_type:
            return jsonify({'error': f"Tipo de hogar invalido: '{raw_type}'"}), 400
        home.type = home_type
        updated_fields.append('type')
        
    if 'approximate_area' in data:
        val = data['approximate_area']
        if val is not None:
            try:
                val = float(val)
                if val < 0:
                    return jsonify({'error': 'approximate_area debe ser positivo'}), 400
            except (ValueError, TypeError):
                return jsonify({'error': 'approximate_area debe ser numerico'}), 400
        home.approximate_area = val
        updated_fields.append('approximate_area')
        
    if 'rooms_count' in data:
        val = data['rooms_count']
        if val is not None:
            try:
                val = int(val)
                if val < 0:
                    return jsonify({'error': 'rooms_count debe ser no negativo'}), 400
            except (ValueError, TypeError):
                return jsonify({'error': 'rooms_count debe ser entero'}), 400
        home.rooms_count = val
        updated_fields.append('rooms_count')

    if not updated_fields:
        return jsonify({'message': 'No se proporcionaron campos para actualizar', 'home': home.to_dict()}), 200

    try:
        home.updated_at = datetime.utcnow()
        
        # Registrar provenance de campos actualizados (commit=False)
        for field in updated_fields:
            if field == 'type':
                record_attribute('home', home.id, 'type', home.type, source='explicit_user_input', confidence=1.000, commit=False)
            elif field == 'approximate_area' and home.approximate_area is not None:
                record_attribute('home', home.id, 'approximate_area', float(home.approximate_area), source='explicit_user_input', confidence=1.000, commit=False)
            elif field == 'rooms_count' and home.rooms_count is not None:
                record_attribute('home', home.id, 'rooms_count', home.rooms_count, source='explicit_user_input', confidence=1.000, commit=False)
            elif field == 'name':
                record_attribute('home', home.id, 'name', home.name, source='explicit_user_input', confidence=1.000, commit=False)
                
        db.session.commit()
        return jsonify({
            'success': True,
            'message': 'Hogar actualizado exitosamente',
            'home': home.to_dict(enriched=True)
        }), 200

    except Exception as e:
        db.session.rollback()
        logger.error(f"Error actualizando hogar #{id}: {e}", exc_info=True)
        return jsonify({'error': 'Error interno al actualizar el hogar'}), 500


@homes_bp.route('/<int:id>', methods=['DELETE'])
@jwt_required()
def delete_home(id):
    """
    DELETE /api/homes/<id>
    Elimina el hogar. Solo el owner legitimo.
    Cascada cubierta por FKs (rooms, assets, members se borran en cascada).
    """
    user_id = int(get_jwt_identity())
    has_access, role, home = _get_membership(id, user_id)
    
    if not home:
        return jsonify({'error': f'Hogar #{id} no encontrado'}), 404
    if home.owner_id != user_id:
        return jsonify({'error': 'Solo el propietario original puede eliminar el hogar'}), 403
        
    try:
        db.session.delete(home)
        db.session.commit()
        return jsonify({
            'success': True,
            'message': f'Hogar #{id} eliminado exitosamente'
        }), 200
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error eliminando hogar #{id}: {e}", exc_info=True)
        return jsonify({'error': 'Error interno al eliminar el hogar'}), 500


# =====================================================================
# 2. HOME ROOMS ENDPOINTS (/api/homes/<home_id>/rooms)
# =====================================================================

@homes_bp.route('/<int:home_id>/rooms', methods=['POST'])
@jwt_required()
def create_room(home_id):
    """
    POST /api/homes/<home_id>/rooms
    Crea una habitacion en el hogar.
    Solo owner o admin. Valida type contra room_type_catalog.
    Registra provenance de type.
    """
    user_id = int(get_jwt_identity())
    has_access, role, home = _get_membership(home_id, user_id)
    
    if not home:
        return jsonify({'error': f'Hogar #{home_id} no encontrado'}), 404
    if not has_access or role not in ('owner', 'admin'):
        return jsonify({'error': 'Se requieren privilegios de propietario o administrador para agregar habitaciones'}), 403
        
    data = request.get_json() or {}
    name = data.get('name')
    if not name or not str(name).strip():
        return jsonify({'error': 'El campo name es obligatorio'}), 400
    name = str(name).strip()
    
    raw_type = data.get('type')
    if not raw_type:
        return jsonify({'error': 'El campo type es obligatorio'}), 400
    raw_type_str = str(raw_type).strip()
    
    # Validacion contra room_type_catalog (por code o name)
    catalog_item = RoomTypeCatalog.query.filter(
        (RoomTypeCatalog.code == raw_type_str.lower()) | (RoomTypeCatalog.name.ilike(raw_type_str))
    ).first()
    if not catalog_item:
        return jsonify({'error': f"Tipo de habitacion '{raw_type_str}' no valido en catalogo"}), 400
    room_code = catalog_item.code

    try:
        room = HomeRoom(
            home_id=home_id,
            name=name,
            type=room_code,
            created_at=datetime.utcnow()
        )
        db.session.add(room)
        db.session.flush()
        
        # Registrar provenance de type (commit=False)
        record_attribute('home_room', room.id, 'type', room.type, source='explicit_user_input', confidence=1.000, commit=False)
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': 'Habitacion creada exitosamente',
            'room': room.to_dict()
        }), 201

    except Exception as e:
        db.session.rollback()
        logger.error(f"Error creando habitacion en hogar #{home_id}: {e}", exc_info=True)
        return jsonify({'error': 'Error interno al crear la habitacion'}), 500


@homes_bp.route('/<int:home_id>/rooms', methods=['GET'])
@jwt_required()
def list_rooms(home_id):
    """
    GET /api/homes/<home_id>/rooms
    Lista habitaciones del hogar. Cualquier miembro/owner puede ver.
    """
    user_id = int(get_jwt_identity())
    has_access, role, home = _get_membership(home_id, user_id)
    
    if not home:
        return jsonify({'error': f'Hogar #{home_id} no encontrado'}), 404
    if not has_access:
        return jsonify({'error': 'No tienes autorizacion para acceder a este hogar'}), 403
        
    rooms = HomeRoom.query.filter_by(home_id=home_id).order_by(HomeRoom.id.asc()).all()
    return jsonify({
        'success': True,
        'home_id': home_id,
        'count': len(rooms),
        'rooms': [r.to_dict() for r in rooms]
    }), 200


@homes_bp.route('/<int:home_id>/rooms/<int:room_id>', methods=['PATCH'])
@jwt_required()
def update_room(home_id, room_id):
    """
    PATCH /api/homes/<home_id>/rooms/<room_id>
    Actualiza una habitacion. Solo owner o admin.
    """
    user_id = int(get_jwt_identity())
    has_access, role, home = _get_membership(home_id, user_id)
    
    if not home:
        return jsonify({'error': f'Hogar #{home_id} no encontrado'}), 404
    if not has_access or role not in ('owner', 'admin'):
        return jsonify({'error': 'Se requieren privilegios de propietario o administrador para modificar habitaciones'}), 403
        
    room = HomeRoom.query.filter_by(id=room_id, home_id=home_id).first()
    if not room:
        return jsonify({'error': f'Habitacion #{room_id} no encontrada en este hogar'}), 404
        
    data = request.get_json() or {}
    updated_fields = []
    
    if 'name' in data:
        new_name = str(data['name']).strip()
        if not new_name:
            return jsonify({'error': 'El nombre no puede estar vacio'}), 400
        room.name = new_name
        updated_fields.append('name')
        
    if 'type' in data:
        raw_type_str = str(data['type']).strip()
        catalog_item = RoomTypeCatalog.query.filter(
            (RoomTypeCatalog.code == raw_type_str.lower()) | (RoomTypeCatalog.name.ilike(raw_type_str))
        ).first()
        if not catalog_item:
            return jsonify({'error': f"Tipo de habitacion '{raw_type_str}' no valido en catalogo"}), 400
        room.type = catalog_item.code
        updated_fields.append('type')

    try:
        if 'type' in updated_fields:
            record_attribute('home_room', room.id, 'type', room.type, source='explicit_user_input', confidence=1.000, commit=False)
        db.session.commit()
            
        return jsonify({
            'success': True,
            'message': 'Habitacion actualizada exitosamente',
            'room': room.to_dict()
        }), 200
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error actualizando habitacion #{room_id}: {e}", exc_info=True)
        return jsonify({'error': 'Error interno al actualizar habitacion'}), 500


@homes_bp.route('/<int:home_id>/rooms/<int:room_id>', methods=['DELETE'])
@jwt_required()
def delete_room(home_id, room_id):
    """
    DELETE /api/homes/<home_id>/rooms/<room_id>
    Elimina una habitacion. Solo owner o admin.
    """
    user_id = int(get_jwt_identity())
    has_access, role, home = _get_membership(home_id, user_id)
    
    if not home:
        return jsonify({'error': f'Hogar #{home_id} no encontrado'}), 404
    if not has_access or role not in ('owner', 'admin'):
        return jsonify({'error': 'Se requieren privilegios de propietario o administrador para eliminar habitaciones'}), 403
        
    room = HomeRoom.query.filter_by(id=room_id, home_id=home_id).first()
    if not room:
        return jsonify({'error': f'Habitacion #{room_id} no encontrada en este hogar'}), 404
        
    try:
        db.session.delete(room)
        db.session.commit()
        return jsonify({
            'success': True,
            'message': f'Habitacion #{room_id} eliminada exitosamente'
        }), 200
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error eliminando habitacion #{room_id}: {e}", exc_info=True)
        return jsonify({'error': 'Error interno al eliminar habitacion'}), 500


# =====================================================================
# 3. HOME ASSETS ENDPOINTS (/api/homes/<home_id>/assets)
# =====================================================================

@homes_bp.route('/<int:home_id>/assets', methods=['POST'])
@jwt_required()
def create_asset(home_id):
    """
    POST /api/homes/<home_id>/assets
    Crea un activo dentro de un hogar.
    Solo owner o admin.
    Valida asset_type contra asset_type_catalog.
    Registra provenance de asset_type, brand, model si se proveen.
    Dispara evento ASSET_CREATED en app_events.
    """
    user_id = int(get_jwt_identity())
    has_access, role, home = _get_membership(home_id, user_id)
    
    if not home:
        return jsonify({'error': f'Hogar #{home_id} no encontrado'}), 404
    if not has_access or role not in ('owner', 'admin'):
        return jsonify({'error': 'Se requieren privilegios de propietario o administrador para agregar activos'}), 403
        
    data = request.get_json() or {}
    
    raw_asset_type = data.get('asset_type')
    if not raw_asset_type:
        return jsonify({'error': 'El campo asset_type es obligatorio'}), 400
    raw_asset_str = str(raw_asset_type).strip()
    
    # Validacion contra asset_type_catalog
    catalog_item = AssetTypeCatalog.query.filter(
        (AssetTypeCatalog.code == raw_asset_str.lower()) | (AssetTypeCatalog.name.ilike(raw_asset_str))
    ).first()
    if not catalog_item:
        return jsonify({'error': f"Tipo de activo '{raw_asset_str}' no valido en catalogo"}), 400
    asset_code = catalog_item.code
    
    # Validar room_id si viene provisto
    room_id = data.get('room_id')
    if room_id is not None:
        try:
            room_id = int(room_id)
            room = HomeRoom.query.filter_by(id=room_id, home_id=home_id).first()
            if not room:
                return jsonify({'error': f"Habitacion #{room_id} no pertenece a este hogar"}), 400
        except (ValueError, TypeError):
            return jsonify({'error': 'room_id debe ser un entero valido'}), 400

    brand = data.get('brand')
    brand = str(brand).strip() if brand else None
    
    model = data.get('model')
    model = str(model).strip() if model else None
    
    def parse_date(date_str, field_name):
        if not date_str:
            return None
        try:
            return datetime.strptime(str(date_str).strip(), '%Y-%m-%d').date()
        except ValueError:
            raise ValueError(f"Formato invalido para {field_name}. Use YYYY-MM-DD")
            
    try:
        installation_date = parse_date(data.get('installation_date'), 'installation_date')
        last_maintenance = parse_date(data.get('last_maintenance'), 'last_maintenance')
        warranty_until = parse_date(data.get('warranty_until'), 'warranty_until')
    except ValueError as ve:
        return jsonify({'error': str(ve)}), 400
        
    metadata = data.get('metadata')
    if metadata is not None and not isinstance(metadata, dict):
        return jsonify({'error': 'metadata debe ser un objeto JSON'}), 400
    metadata = metadata or {}

    try:
        asset = HomeAsset(
            home_id=home_id,
            room_id=room_id,
            asset_type=asset_code,
            brand=brand,
            model=model,
            installation_date=installation_date,
            last_maintenance=last_maintenance,
            warranty_until=warranty_until,
            metadata_=metadata,
            created_at=datetime.utcnow(),
            updated_at=datetime.utcnow()
        )
        db.session.add(asset)
        db.session.flush()
        
        # Registrar provenance de campos provistos (commit=False)
        record_attribute('home_asset', asset.id, 'asset_type', asset.asset_type, source='explicit_user_input', confidence=1.000, commit=False)
        if brand:
            record_attribute('home_asset', asset.id, 'brand', brand, source='explicit_user_input', confidence=1.000, commit=False)
        if model:
            record_attribute('home_asset', asset.id, 'model', model, source='explicit_user_input', confidence=1.000, commit=False)
            
        db.session.commit()

        # Disparar evento ASSET_CREATED (FASE 6 event tracking)
        try:
            track_event(
                event_type='ASSET_CREATED',
                user_id=user_id,
                entity_type='home_asset',
                entity_id=asset.id,
                metadata={
                    'home_id': home_id,
                    'room_id': asset.room_id,
                    'asset_type': asset.asset_type,
                    'brand': asset.brand,
                    'model': asset.model
                }
            )
        except Exception as ev_err:
            logger.warning(f"No se pudo disparar evento ASSET_CREATED: {ev_err}")

        return jsonify({
            'success': True,
            'message': 'Activo creado exitosamente',
            'asset': asset.to_dict()
        }), 201

    except Exception as e:
        db.session.rollback()
        logger.error(f"Error creando activo en hogar #{home_id}: {e}", exc_info=True)
        return jsonify({'error': 'Error interno al crear el activo'}), 500


@homes_bp.route('/<int:home_id>/assets', methods=['GET'])
@jwt_required()
def list_assets(home_id):
    """
    GET /api/homes/<home_id>/assets
    Lista activos de un hogar. Filtro opcional ?room_id=X.
    Cualquier miembro/owner puede ver.
    """
    user_id = int(get_jwt_identity())
    has_access, role, home = _get_membership(home_id, user_id)
    
    if not home:
        return jsonify({'error': f'Hogar #{home_id} no encontrado'}), 404
    if not has_access:
        return jsonify({'error': 'No tienes autorizacion para acceder a este hogar'}), 403
        
    query = HomeAsset.query.filter_by(home_id=home_id)
    room_id = request.args.get('room_id', type=int)
    if room_id is not None:
        query = query.filter_by(room_id=room_id)
        
    assets = query.order_by(HomeAsset.id.asc()).all()
    return jsonify({
        'success': True,
        'home_id': home_id,
        'count': len(assets),
        'assets': [a.to_dict() for a in assets]
    }), 200


@homes_bp.route('/<int:home_id>/assets/<int:asset_id>', methods=['PATCH'])
@jwt_required()
def update_asset(home_id, asset_id):
    """
    PATCH /api/homes/<home_id>/assets/<asset_id>
    Actualiza un activo. Solo owner o admin.
    Registra provenance de los campos cambiados.
    """
    user_id = int(get_jwt_identity())
    has_access, role, home = _get_membership(home_id, user_id)
    
    if not home:
        return jsonify({'error': f'Hogar #{home_id} no encontrado'}), 404
    if not has_access or role not in ('owner', 'admin'):
        return jsonify({'error': 'Se requieren privilegios de propietario o administrador para modificar activos'}), 403
        
    asset = HomeAsset.query.filter_by(id=asset_id, home_id=home_id).first()
    if not asset:
        return jsonify({'error': f'Activo #{asset_id} no encontrado en este hogar'}), 404
        
    data = request.get_json() or {}
    updated_provenance = []
    
    if 'asset_type' in data:
        raw_asset_str = str(data['asset_type']).strip()
        catalog_item = AssetTypeCatalog.query.filter(
            (AssetTypeCatalog.code == raw_asset_str.lower()) | (AssetTypeCatalog.name.ilike(raw_asset_str))
        ).first()
        if not catalog_item:
            return jsonify({'error': f"Tipo de activo '{raw_asset_str}' no valido en catalogo"}), 400
        asset.asset_type = catalog_item.code
        updated_provenance.append(('asset_type', asset.asset_type))
        
    if 'room_id' in data:
        room_id = data['room_id']
        if room_id is not None:
            try:
                room_id = int(room_id)
                room = HomeRoom.query.filter_by(id=room_id, home_id=home_id).first()
                if not room:
                    return jsonify({'error': f"Habitacion #{room_id} no pertenece a este hogar"}), 400
            except (ValueError, TypeError):
                return jsonify({'error': 'room_id debe ser un entero valido'}), 400
        asset.room_id = room_id
        
    if 'brand' in data:
        asset.brand = str(data['brand']).strip() if data['brand'] else None
        updated_provenance.append(('brand', asset.brand))
        
    if 'model' in data:
        asset.model = str(data['model']).strip() if data['model'] else None
        updated_provenance.append(('model', asset.model))
        
    def parse_date(date_str, field_name):
        if not date_str:
            return None
        try:
            return datetime.strptime(str(date_str).strip(), '%Y-%m-%d').date()
        except ValueError:
            raise ValueError(f"Formato invalido para {field_name}. Use YYYY-MM-DD")
            
    try:
        if 'installation_date' in data:
            asset.installation_date = parse_date(data['installation_date'], 'installation_date')
        if 'last_maintenance' in data:
            asset.last_maintenance = parse_date(data['last_maintenance'], 'last_maintenance')
        if 'warranty_until' in data:
            asset.warranty_until = parse_date(data['warranty_until'], 'warranty_until')
    except ValueError as ve:
        return jsonify({'error': str(ve)}), 400
        
    if 'metadata' in data:
        meta = data['metadata']
        if meta is not None and not isinstance(meta, dict):
            return jsonify({'error': 'metadata debe ser un objeto JSON'}), 400
        asset.metadata_ = meta or {}

    try:
        asset.updated_at = datetime.utcnow()
        
        # Registrar provenance de campos actualizados (commit=False)
        for key, val in updated_provenance:
            if val is not None:
                record_attribute('home_asset', asset.id, key, val, source='explicit_user_input', confidence=1.000, commit=False)
                
        db.session.commit()
        return jsonify({
            'success': True,
            'message': 'Activo actualizado exitosamente',
            'asset': asset.to_dict()
        }), 200

    except Exception as e:
        db.session.rollback()
        logger.error(f"Error actualizando activo #{asset_id}: {e}", exc_info=True)
        return jsonify({'error': 'Error interno al actualizar el activo'}), 500


@homes_bp.route('/<int:home_id>/assets/<int:asset_id>', methods=['DELETE'])
@jwt_required()
def delete_asset(home_id, asset_id):
    """
    DELETE /api/homes/<home_id>/assets/<asset_id>
    Elimina un activo. Solo owner o admin.
    """
    user_id = int(get_jwt_identity())
    has_access, role, home = _get_membership(home_id, user_id)
    
    if not home:
        return jsonify({'error': f'Hogar #{home_id} no encontrado'}), 404
    if not has_access or role not in ('owner', 'admin'):
        return jsonify({'error': 'Se requieren privilegios de propietario o administrador para eliminar activos'}), 403
        
    asset = HomeAsset.query.filter_by(id=asset_id, home_id=home_id).first()
    if not asset:
        return jsonify({'error': f'Activo #{asset_id} no encontrado en este hogar'}), 404
        
    try:
        db.session.delete(asset)
        db.session.commit()
        return jsonify({
            'success': True,
            'message': f'Activo #{asset_id} eliminado exitosamente'
        }), 200
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error eliminando activo #{asset_id}: {e}", exc_info=True)
        return jsonify({'error': 'Error interno al eliminar activo'}), 500
'''

with open(TARGET_PATH, "w", encoding="utf-8") as f:
    f.write(content.strip() + "\n")

print(f"[OK] homes.py actualizado exitosamente en {TARGET_PATH}")
