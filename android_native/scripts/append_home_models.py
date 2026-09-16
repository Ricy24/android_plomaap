import os
import shutil

TARGET = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\database\models.py"
BACKUP = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\database\models.py.bak_fase7"

shutil.copy2(TARGET, BACKUP)
print(f"[OK] Backup creado en {BACKUP}")

with open(TARGET, 'r', encoding='utf-8') as f:
    content = f.read()

models_code = '''

class RoomTypeCatalog(db.Model):
    """Catalog of Room Types (FASE 5)"""
    __tablename__ = 'room_type_catalog'

    id = db.Column(db.Integer, primary_key=True)
    code = db.Column(db.String(50), unique=True, nullable=False)
    name = db.Column(db.String(100), nullable=False)
    description = db.Column(db.Text, nullable=True)
    icon = db.Column(db.String(50), nullable=True)
    display_order = db.Column(db.Integer, default=0, nullable=False)
    is_active = db.Column(db.Boolean, default=True, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    def to_dict(self):
        return {
            'id': self.id,
            'code': self.code,
            'name': self.name,
            'description': self.description,
            'icon': self.icon,
            'display_order': self.display_order,
            'is_active': self.is_active
        }


class AssetTypeCatalog(db.Model):
    """Catalog of Asset Types (FASE 5)"""
    __tablename__ = 'asset_type_catalog'

    id = db.Column(db.Integer, primary_key=True)
    code = db.Column(db.String(50), unique=True, nullable=False)
    name = db.Column(db.String(100), nullable=False)
    category = db.Column(db.String(50), nullable=False)
    icon = db.Column(db.String(50), nullable=True)
    default_maintenance_months = db.Column(db.Integer, nullable=True)
    display_order = db.Column(db.Integer, default=0, nullable=False)
    is_active = db.Column(db.Boolean, default=True, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    def to_dict(self):
        return {
            'id': self.id,
            'code': self.code,
            'name': self.name,
            'category': self.category,
            'icon': self.icon,
            'default_maintenance_months': self.default_maintenance_months,
            'display_order': self.display_order,
            'is_active': self.is_active
        }


class Home(db.Model):
    """Digital Home Entity (FASE 5)"""
    __tablename__ = 'homes'
    __table_args__ = (
        db.Index('idx_homes_owner', 'owner_id'),
    )

    id = db.Column(db.Integer, primary_key=True)
    owner_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), nullable=False)
    name = db.Column(db.String(120), nullable=False)
    type = db.Column(db.String(30), default='apartment', nullable=False)
    approximate_area = db.Column(db.Numeric(8, 2), nullable=True)
    rooms_count = db.Column(db.Integer, default=0, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    owner = db.relationship('User', backref=db.backref('owned_homes', cascade='all, delete-orphan'))
    members = db.relationship('HomeMember', backref='home', cascade='all, delete-orphan', lazy='dynamic')
    rooms = db.relationship('HomeRoom', backref='home', cascade='all, delete-orphan', lazy='dynamic')
    assets = db.relationship('HomeAsset', backref='home', cascade='all, delete-orphan', lazy='dynamic')

    def to_dict(self, enriched=False):
        data = {
            'id': self.id,
            'owner_id': self.owner_id,
            'name': self.name,
            'type': self.type,
            'approximate_area': float(self.approximate_area) if self.approximate_area is not None else None,
            'rooms_count': self.rooms_count,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }
        if enriched:
            data['members_count'] = self.members.count()
            data['actual_rooms_count'] = self.rooms.count()
            data['assets_count'] = self.assets.count()
        return data


class HomeMember(db.Model):
    """Membership and Role in a Digital Home (FASE 5)"""
    __tablename__ = 'home_members'
    __table_args__ = (
        db.UniqueConstraint('home_id', 'user_id', name='uq_home_member'),
        db.Index('idx_home_members_home', 'home_id'),
        db.Index('idx_home_members_user', 'user_id'),
    )

    id = db.Column(db.Integer, primary_key=True)
    home_id = db.Column(db.Integer, db.ForeignKey('homes.id', ondelete='CASCADE'), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id', ondelete='CASCADE'), nullable=False)
    role = db.Column(db.String(30), default='member', nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    user = db.relationship('User', backref=db.backref('home_memberships', cascade='all, delete-orphan'))

    def to_dict(self):
        return {
            'id': self.id,
            'home_id': self.home_id,
            'user_id': self.user_id,
            'user_name': self.user.name if self.user else None,
            'user_email': self.user.email if self.user else None,
            'role': self.role,
            'created_at': self.created_at.isoformat() if self.created_at else None
        }


class HomeRoom(db.Model):
    """Room in a Digital Home (FASE 5)"""
    __tablename__ = 'home_rooms'
    __table_args__ = (
        db.Index('idx_home_rooms_home', 'home_id'),
        db.Index('idx_home_rooms_type', 'type'),
    )

    id = db.Column(db.Integer, primary_key=True)
    home_id = db.Column(db.Integer, db.ForeignKey('homes.id', ondelete='CASCADE'), nullable=False)
    name = db.Column(db.String(100), nullable=False)
    type = db.Column(db.String(50), db.ForeignKey('room_type_catalog.code', ondelete='RESTRICT', onupdate='CASCADE'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    assets = db.relationship('HomeAsset', backref='room', lazy='dynamic')
    room_type = db.relationship('RoomTypeCatalog', foreign_keys=[type], primaryjoin='HomeRoom.type == RoomTypeCatalog.code')

    def to_dict(self):
        return {
            'id': self.id,
            'home_id': self.home_id,
            'name': self.name,
            'type': self.type,
            'type_name': self.room_type.name if self.room_type else self.type,
            'icon': self.room_type.icon if self.room_type else None,
            'assets_count': self.assets.count(),
            'created_at': self.created_at.isoformat() if self.created_at else None
        }


class HomeAsset(db.Model):
    """Asset / Appliance in a Digital Home (FASE 5)"""
    __tablename__ = 'home_assets'
    __table_args__ = (
        db.Index('idx_home_assets_home', 'home_id'),
        db.Index('idx_home_assets_room', 'room_id'),
        db.Index('idx_home_assets_type', 'asset_type'),
    )

    id = db.Column(db.Integer, primary_key=True)
    home_id = db.Column(db.Integer, db.ForeignKey('homes.id', ondelete='CASCADE'), nullable=False)
    room_id = db.Column(db.Integer, db.ForeignKey('home_rooms.id', ondelete='SET NULL'), nullable=True)
    asset_type = db.Column(db.String(50), db.ForeignKey('asset_type_catalog.code', ondelete='RESTRICT', onupdate='CASCADE'), nullable=False)
    brand = db.Column(db.String(100), nullable=True)
    model = db.Column(db.String(100), nullable=True)
    installation_date = db.Column(db.Date, nullable=True)
    last_maintenance = db.Column(db.Date, nullable=True)
    warranty_until = db.Column(db.Date, nullable=True)
    metadata_ = db.Column('metadata', db.JSON, default=dict)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    asset_type_obj = db.relationship('AssetTypeCatalog', foreign_keys=[asset_type], primaryjoin='HomeAsset.asset_type == AssetTypeCatalog.code')

    def to_dict(self):
        return {
            'id': self.id,
            'home_id': self.home_id,
            'room_id': self.room_id,
            'room_name': self.room.name if self.room else None,
            'asset_type': self.asset_type,
            'asset_type_name': self.asset_type_obj.name if self.asset_type_obj else self.asset_type,
            'category': self.asset_type_obj.category if self.asset_type_obj else None,
            'brand': self.brand,
            'model': self.model,
            'installation_date': self.installation_date.isoformat() if self.installation_date else None,
            'last_maintenance': self.last_maintenance.isoformat() if self.last_maintenance else None,
            'warranty_until': self.warranty_until.isoformat() if self.warranty_until else None,
            'metadata': self.metadata_ or {},
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }


class ProfileAttribute(db.Model):
    """Profile Attributes with Source Provenance and Confidence (FASE 7)"""
    __tablename__ = 'profile_attributes'
    __table_args__ = (
        db.UniqueConstraint('entity_type', 'entity_id', 'attribute_key', name='uq_profile_attribute'),
        db.Index('idx_profile_attributes_entity', 'entity_type', 'entity_id'),
        db.Index('idx_profile_attributes_source', 'source'),
    )

    id = db.Column(db.BigInteger, primary_key=True)
    entity_type = db.Column(db.String(30), nullable=False)
    entity_id = db.Column(db.Integer, nullable=False)
    attribute_key = db.Column(db.String(100), nullable=False)
    attribute_value = db.Column(db.JSON, nullable=False)
    source = db.Column(db.String(30), nullable=False)
    confidence = db.Column(db.Numeric(4, 3), default=1.000, nullable=False)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    def to_dict(self):
        return {
            'id': self.id,
            'entity_type': self.entity_type,
            'entity_id': self.entity_id,
            'attribute_key': self.attribute_key,
            'attribute_value': self.attribute_value,
            'source': self.source,
            'confidence': float(self.confidence) if self.confidence is not None else 1.0,
            'updated_at': self.updated_at.isoformat() if self.updated_at else None
        }
'''

with open(TARGET, 'w', encoding='utf-8') as f:
    f.write(content.rstrip() + "\n" + models_code)

print("[OK] Modelos de Hogar Digital y ProfileAttribute agregados a models.py.")
