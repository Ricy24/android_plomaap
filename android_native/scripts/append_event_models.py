import os
import shutil

TARGET = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\database\models.py"
BACKUP = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\database\models.py.bak_fase6"

shutil.copy2(TARGET, BACKUP)
print(f"[OK] Backup creado en {BACKUP}")

with open(TARGET, 'r', encoding='utf-8') as f:
    content = f.read()

models_code = '''

class AppEvent(db.Model):
    """Application Event Log for User Actions and System Events"""
    __tablename__ = 'app_events'
    __table_args__ = (
        db.Index('idx_app_events_user', 'user_id'),
        db.Index('idx_app_events_type', 'event_type'),
        db.Index('idx_app_events_entity', 'entity_type', 'entity_id'),
        db.Index('idx_app_events_created', 'created_at'),
    )

    id = db.Column(db.BigInteger, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='SET NULL'), nullable=True)
    session_id = db.Column(db.String(100), nullable=True)
    event_type = db.Column(db.String(50), nullable=False)
    entity_type = db.Column(db.String(50), nullable=True)
    entity_id = db.Column(db.Integer, nullable=True)
    metadata_ = db.Column('metadata', db.JSON, default=dict)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    user = db.relationship('User', backref=db.backref('app_events', lazy='dynamic'))

    def to_dict(self):
        return {
            'id': self.id,
            'user_id': self.user_id,
            'session_id': self.session_id,
            'event_type': self.event_type,
            'entity_type': self.entity_type,
            'entity_id': self.entity_id,
            'metadata': self.metadata_ or {},
            'created_at': self.created_at.isoformat() if self.created_at else None
        }


class RecommendationEvent(db.Model):
    """Recommendation Engine Event Log (FASE 12 ready)"""
    __tablename__ = 'recommendation_events'
    __table_args__ = (
        db.Index('idx_recommendation_events_user', 'user_id'),
        db.Index('idx_recommendation_events_type', 'recommendation_type'),
    )

    id = db.Column(db.BigInteger, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), nullable=False)
    recommendation_type = db.Column(db.String(50), nullable=False)
    entity_id = db.Column(db.Integer, nullable=False)
    score = db.Column(db.Numeric(6, 4), nullable=True)
    algorithm_version = db.Column(db.String(30), nullable=False)
    position = db.Column(db.Integer, nullable=True)
    shown_at = db.Column(db.DateTime, nullable=True)
    clicked_at = db.Column(db.DateTime, nullable=True)
    converted_at = db.Column(db.DateTime, nullable=True)
    metadata_ = db.Column('metadata', db.JSON, default=dict)

    user = db.relationship('User', backref=db.backref('recommendation_events', lazy='dynamic'))

    def to_dict(self):
        return {
            'id': self.id,
            'user_id': self.user_id,
            'recommendation_type': self.recommendation_type,
            'entity_id': self.entity_id,
            'score': float(self.score) if self.score is not None else None,
            'algorithm_version': self.algorithm_version,
            'position': self.position,
            'shown_at': self.shown_at.isoformat() if self.shown_at else None,
            'clicked_at': self.clicked_at.isoformat() if self.clicked_at else None,
            'converted_at': self.converted_at.isoformat() if self.converted_at else None,
            'metadata': self.metadata_ or {}
        }
'''

with open(TARGET, 'w', encoding='utf-8') as f:
    f.write(content.rstrip() + "\n" + models_code)

print("[OK] Modelos AppEvent y RecommendationEvent agregados a models.py exitosamente.")
