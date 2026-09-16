import os

content = """import logging
from datetime import datetime, timedelta
from app.database.extensions import db
from app.database.models import (
    Service, AppEvent, RecommendationEvent, 
    HomeAsset, AssetTypeCatalog, ProfileAttribute, Appointment
)

logger = logging.getLogger(__name__)

RECOMMENDATION_WEIGHTS = {
    'asset_match': 0.35,
    'preference_match': 0.25,
    'history_match': 0.15,
    'popularity': 0.05,
    'repetition_penalty': 0.20
}

def generate_recommendations(user_id=None, limit=5):
    try:
        services = Service.query.filter_by().all()
        
        user_assets = []
        user_prefs = []
        user_events = []
        user_appointments = []
        
        if user_id:
            sql = '''
            SELECT ac.code, ac.name, ac.category 
            FROM asset_type_catalog ac
            JOIN home_assets ha ON ha.asset_type_id = ac.id
            JOIN home_rooms hr ON ha.room_id = hr.id
            JOIN homes h ON hr.home_id = h.id
            JOIN home_members hm ON hm.home_id = h.id
            WHERE hm.user_id = :user_id AND h.is_active = True
            '''
            result = db.session.execute(db.text(sql), {"user_id": user_id}).fetchall()
            user_assets = [r[0] for r in result]
            
            events = AppEvent.query.filter_by(user_id=user_id).order_by(AppEvent.created_at.desc()).limit(20).all()
            user_events = [e for e in events]
            
            user_appointments = Appointment.query.filter_by(user_id=user_id).order_by(Appointment.created_at.desc()).limit(10).all()

        popularity_sql = "SELECT entity_id, COUNT(*) as cnt FROM app_events WHERE event_type IN ('SERVICE_BOOKED', 'BOOKING_CREATED') AND entity_type = 'service' GROUP BY entity_id"
        pop_result = db.session.execute(db.text(popularity_sql)).fetchall()
        pop_map = {r[0]: r[1] for r in pop_result}
        max_pop = max(pop_map.values()) if pop_map else 1

        candidates = []
        for svc in services:
            score = 0.0
            factors = {}
            reasons = []
            
            s_pop = pop_map.get(svc.id, 0) / max_pop
            score += RECOMMENDATION_WEIGHTS['popularity'] * s_pop
            factors['popularity'] = round(s_pop, 4)
            
            if user_id:
                s_asset = 0.0
                asset_matched = False
                if svc.id == 1:
                    if any(a in ['ducha', 'lavamanos', 'sanitario', 'calentador', 'lavadora'] for a in user_assets):
                        s_asset = 1.0
                        asset_matched = True
                        reasons.append("Recomendado porque tienes equipos de plomería en tu Hogar Digital.")
                elif svc.id == 2:
                    if any(a in ['sanitario', 'lavamanos', 'ducha'] for a in user_assets):
                        s_asset = 1.0
                        asset_matched = True
                        reasons.append("Servicio esencial para el mantenimiento preventivo de tus baños.")
                elif svc.id == 3:
                    if any(a in ['ducha', 'sanitario', 'lavamanos'] for a in user_assets):
                        s_asset = 1.0
                        asset_matched = True
                        reasons.append("Ideal para el cuidado de los baños registrados en tu hogar.")
                elif svc.id == 4:
                    if any(a in ['calentador', 'lavadora', 'aire_acondicionado'] for a in user_assets):
                        s_asset = 1.0
                        asset_matched = True
                        reasons.append("Recomendado por los electrodomésticos y equipos de gas en tu hogar.")
                
                score += RECOMMENDATION_WEIGHTS['asset_match'] * s_asset
                factors['asset_match'] = round(s_asset, 4)
                
                s_hist = 0.0
                views = sum(1 for e in user_events if e.event_type in ('SERVICE_VIEWED', 'SERVICE_CLICKED') and e.entity_id == svc.id)
                if views > 0:
                    s_hist = min(views / 5.0, 1.0)
                    reasons.append("Basado en tus búsquedas e intereses recientes.")
                
                score += RECOMMENDATION_WEIGHTS['history_match'] * s_hist
                factors['history_match'] = round(s_hist, 4)
                
                penalty = 0.0
                recent_booking = next((a for a in user_appointments if a.service_id == svc.id and a.created_at > datetime.utcnow() - timedelta(days=30)), None)
                if recent_booking:
                    penalty = 1.0
                    reasons.append("Agendaste este servicio recientemente.")
                
                score -= RECOMMENDATION_WEIGHTS['repetition_penalty'] * penalty
                factors['repetition_penalty'] = round(penalty, 4)

            if not reasons:
                reasons.append("Servicio destacado y altamente calificado por la comunidad.")
                
            candidates.append({
                'service': svc,
                'score': round(score, 4),
                'factors': factors,
                'reason': reasons[0] if reasons else ""
            })
            
        candidates.sort(key=lambda x: x['score'], reverse=True)
        final_recommendations = candidates[:limit]
        
        result_items = []
        for idx, rec in enumerate(final_recommendations):
            svc = rec['service']
            
            rec_id = None
            if user_id:
                rec_event = RecommendationEvent(
                    user_id=user_id,
                    recommendation_type='service',
                    entity_id=svc.id,
                    score=rec['score'],
                    algorithm_version='recommendation_v1',
                    position=idx + 1,
                    shown_at=datetime.utcnow(),
                    metadata_={'reason': rec['reason'], 'factors': rec['factors']}
                )
                db.session.add(rec_event)
                db.session.flush()
                rec_id = rec_event.id
            
            item = svc.to_dict()
            item['recommendation_id'] = rec_id
            item['recommendation_score'] = rec['score']
            item['recommendation_reason'] = rec['reason']
            result_items.append(item)
            
        if user_id:
            db.session.commit()
            
        return result_items

    except Exception as e:
        db.session.rollback()
        logger.error(f"Error generating recommendations: {e}", exc_info=True)
        services = Service.query.limit(limit).all()
        return [{
            **svc.to_dict(),
            'recommendation_id': None,
            'recommendation_score': 0.0,
            'recommendation_reason': "Recomendación general."
        } for svc in services]

def track_recommendation_click(rec_id, user_id):
    try:
        rec = RecommendationEvent.query.filter_by(id=rec_id, user_id=user_id).first()
        if rec and not rec.clicked_at:
            rec.clicked_at = datetime.utcnow()
            
            event = AppEvent(
                user_id=user_id,
                event_type='SERVICE_CLICKED',
                entity_type='service',
                entity_id=rec.entity_id,
                metadata_={'recommendation_id': rec_id, 'source': 'ai_recommendation'},
                created_at=datetime.utcnow()
            )
            db.session.add(event)
            db.session.commit()
            return True
        return False
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error tracking click: {e}")
        return False

def track_recommendation_conversion(rec_id, user_id):
    try:
        rec = RecommendationEvent.query.filter_by(id=rec_id, user_id=user_id).first()
        if rec and not rec.converted_at:
            rec.converted_at = datetime.utcnow()
            db.session.commit()
            return True
        return False
    except Exception as e:
        db.session.rollback()
        logger.error(f"Error tracking conversion: {e}")
        return False
"""

target = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\services\recommendation_service.py"
with open(target, 'w', encoding='utf-8') as f:
    f.write(content)
print("Created recommendation_service.py")
