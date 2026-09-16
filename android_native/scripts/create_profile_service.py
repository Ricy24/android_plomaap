import os
import sys

TARGET_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\services\profile_service.py"

content = '''"""
Profile Attribute and Provenance Tracking Service for PlomApp
FASE 7: Perfil de Hogar (Infraestructura de Fuente y Confianza)
"""
import logging
from datetime import datetime, date
from decimal import Decimal
from sqlalchemy.dialects.postgresql import insert
from app.database.extensions import db
from app.database.models import ProfileAttribute

logger = logging.getLogger(__name__)

VALID_SOURCES = {
    'onboarding',
    'explicit_user_input',
    'search',
    'booking',
    'rating',
    'home_asset',
    'inferred',
    'system'
}


def _normalize_json_value(val):
    """Convierte tipos no serializables nativamente (Decimal, date, datetime) a tipos JSON compatibles."""
    if isinstance(val, Decimal):
        f = float(val)
        return int(f) if f.is_integer() else f
    if isinstance(val, (datetime, date)):
        return val.isoformat()
    if isinstance(val, dict):
        return {str(k): _normalize_json_value(v) for k, v in val.items()}
    if isinstance(val, (list, tuple, set)):
        return [_normalize_json_value(x) for x in val]
    return val


def record_attribute(entity_type, entity_id, attribute_key, attribute_value, source='explicit_user_input', confidence=1.0, commit=True):
    """
    Registra o actualiza (upsert) un atributo de perfil con fuente y confianza en public.profile_attributes.
    
    :param entity_type: Tipo de entidad ('home', 'home_room', 'home_asset')
    :param entity_id: ID entero de la entidad
    :param attribute_key: Clave del atributo ('type', 'brand', 'approximate_area', etc.)
    :param attribute_value: Valor del atributo (serializable a JSONB)
    :param source: Fuente del dato (debe pertenecer a VALID_SOURCES)
    :param confidence: Nivel de confianza numerico entre 0.0 y 1.0
    :param commit: Si True, realiza commit en la base de datos
    :return: True o lanza excepcion
    """
    if source not in VALID_SOURCES:
        raise ValueError(f"Fuente '{source}' no valida. Debe ser una de: {sorted(list(VALID_SOURCES))}")
    
    conf_num = Decimal(str(confidence))
    if conf_num < Decimal('0.0') or conf_num > Decimal('1.0'):
        raise ValueError(f"Confianza {confidence} fuera de rango [0.0, 1.0]")
        
    normalized_value = _normalize_json_value(attribute_value)
    now = datetime.utcnow()
    
    stmt = insert(ProfileAttribute).values(
        entity_type=str(entity_type),
        entity_id=int(entity_id),
        attribute_key=str(attribute_key),
        attribute_value=normalized_value,
        source=str(source),
        confidence=conf_num,
        updated_at=now
    )
    stmt = stmt.on_conflict_do_update(
        constraint='uq_profile_attribute',
        set_={
            'attribute_value': stmt.excluded.attribute_value,
            'source': stmt.excluded.source,
            'confidence': stmt.excluded.confidence,
            'updated_at': stmt.excluded.updated_at
        }
    )
    
    try:
        db.session.execute(stmt)
        if commit:
            db.session.commit()
        return True
    except Exception as e:
        if commit:
            db.session.rollback()
        logger.error(f"Error registrando atributo {entity_type}:{entity_id}:{attribute_key}: {e}", exc_info=True)
        raise e

def get_entity_attributes(entity_type, entity_id):
    """
    Obtiene todos los atributos registrados para una entidad en formato de diccionario clave -> dict.
    """
    rows = ProfileAttribute.query.filter_by(
        entity_type=str(entity_type),
        entity_id=int(entity_id)
    ).all()
    return {row.attribute_key: row.to_dict() for row in rows}

def delete_entity_attributes(entity_type, entity_id, commit=True):
    """
    Elimina atributos de una entidad eliminada.
    """
    try:
        count = ProfileAttribute.query.filter_by(
            entity_type=str(entity_type),
            entity_id=int(entity_id)
        ).delete()
        if commit:
            db.session.commit()
        return count
    except Exception as e:
        if commit:
            db.session.rollback()
        logger.error(f"Error eliminando atributos para {entity_type}:{entity_id}: {e}", exc_info=True)
        raise e
'''

with open(TARGET_PATH, "w", encoding="utf-8") as f:
    f.write(content.strip() + "\n")

print(f"[OK] profile_service.py actualizado exitosamente en {TARGET_PATH}")
