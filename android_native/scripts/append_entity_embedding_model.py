import os
import shutil

MODELS_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\database\models.py"
BACKUP_PATH = MODELS_PATH + ".bak_fase9"

if not os.path.exists(BACKUP_PATH):
    shutil.copy2(MODELS_PATH, BACKUP_PATH)
    print(f"[OK] Backup creado en {BACKUP_PATH}")

with open(MODELS_PATH, "r", encoding="utf-8") as f:
    code = f.read()

if "class EntityEmbedding(db.Model):" not in code:
    model_code = '''

class EntityEmbedding(db.Model):
    """Vector Embeddings Storage for Semantic Search (FASE 9)"""
    __tablename__ = 'entity_embeddings'
    __table_args__ = (
        db.UniqueConstraint('entity_type', 'entity_id', 'model_name', name='uq_entity_embedding'),
        db.Index('idx_entity_embeddings_lookup', 'entity_type', 'entity_id'),
    )

    id = db.Column(db.Integer, primary_key=True)
    entity_type = db.Column(db.String(50), nullable=False)
    entity_id = db.Column(db.Integer, nullable=False)
    content_text = db.Column(db.Text, nullable=False)
    # Almacenado como texto en SQLAlchemy y casteado a vector(512) en PostgreSQL
    embedding = db.Column(db.Text, nullable=True)
    model_name = db.Column(db.String(100), nullable=False)
    dimension = db.Column(db.Integer, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    def to_dict(self, include_vector=False):
        data = {
            'id': self.id,
            'entity_type': self.entity_type,
            'entity_id': self.entity_id,
            'content_text': self.content_text,
            'model_name': self.model_name,
            'dimension': self.dimension,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }
        if include_vector and self.embedding:
            data['embedding'] = self.embedding
        return data
'''
    code += model_code
    with open(MODELS_PATH, "w", encoding="utf-8") as f:
        f.write(code)
    print("[OK] EntityEmbedding añadido exitosamente a models.py")
else:
    print("[INFO] EntityEmbedding ya existe en models.py")
