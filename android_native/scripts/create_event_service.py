import os

TARGET = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\services\event_service.py"

content = '''"""
Event Tracking Service for PlomApp
FASE 6: Event Tracking
"""
import logging
from datetime import datetime
from app.database.extensions import db
from app.database.models import AppEvent

logger = logging.getLogger(__name__)

# Catálogo oficial de tipos de eventos validados a nivel de aplicación
VALID_EVENT_TYPES = {
    'USER_REGISTERED',
    'ONBOARDING_STARTED',
    'ONBOARDING_COMPLETED',
    'SEARCH_PERFORMED',
    'SERVICE_VIEWED',
    'SERVICE_RECOMMENDED',
    'SERVICE_CLICKED',
    'SERVICE_BOOKED',
    'BOOKING_CREATED',
    'BOOKING_CANCELLED',
    'BOOKING_COMPLETED',
    'RATING_CREATED',
    'ASSET_CREATED',
    'MAINTENANCE_RECORDED',
    'NOTIFICATION_CREATED',
    'NOTIFICATION_OPENED',
    'NOTIFICATION_ACTIONED'
}

def track_event(event_type, user_id=None, entity_type=None, entity_id=None, session_id=None, metadata=None, strict=False):
    """
    Registra un evento de aplicación en public.app_events.
    
    :param event_type: Tipo de evento (debe pertenecer a VALID_EVENT_TYPES)
    :param user_id: ID del usuario (opcional)
    :param entity_type: Tipo de entidad relacionada (ej. 'user', 'appointment', 'service')
    :param entity_id: ID de la entidad relacionada
    :param session_id: Identificador de sesión cliente/dispositivo (opcional)
    :param metadata: Diccionario con información contextual (JSON)
    :param strict: Si True, lanza ValueError ante tipo de evento no válido
    :return: Instancia de AppEvent creada, o None en caso de fallo no estricto
    """
    if event_type not in VALID_EVENT_TYPES:
        msg = f"Tipo de evento inválido: '{event_type}'. Debe pertenecer a VALID_EVENT_TYPES."
        logger.warning(msg)
        if strict:
            raise ValueError(msg)
        return None

    try:
        event = AppEvent(
            user_id=user_id,
            session_id=session_id,
            event_type=event_type,
            entity_type=entity_type,
            entity_id=entity_id,
            metadata_=metadata or {},
            created_at=datetime.utcnow()
        )
        db.session.add(event)
        db.session.commit()
        return event
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error registrando evento '{event_type}': {e}", exc_info=True)
        if strict:
            raise e
        return None
'''

with open(TARGET, 'w', encoding='utf-8') as f:
    f.write(content.strip() + "\n")

print(f"[OK] Creado {TARGET} exitosamente.")
