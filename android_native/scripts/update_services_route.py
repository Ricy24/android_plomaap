import os

TARGET_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\routes\services.py"

content = '''from flask import Blueprint, jsonify, request
from sqlalchemy import or_
from app.database.models import Service
from app.database.extensions import db

services_bp = Blueprint('services', __name__, url_prefix='/api/services')


@services_bp.route('', methods=['GET'])
def list_services():
    """List all services with pagination, category filter and hybrid search"""
    limit = request.args.get('limit', default=50, type=int)
    offset = request.args.get('offset', default=0, type=int)
    search = request.args.get('search', default='', type=str).strip()
    category = request.args.get('category', default='', type=str).strip()

    if limit > 100:
        limit = 100

    # Búsqueda híbrida inteligente si se proporciona texto de búsqueda
    if search:
        try:
            from app.services.hybrid_search_service import perform_hybrid_search
            cat_filter = category if category.lower() not in ['todo', 'todos', ''] else None
            hybrid_res = perform_hybrid_search(
                query=search,
                category_filter=cat_filter,
                top_k=limit
            )
            services_data = []
            for item in hybrid_res.get('results', []):
                s_dict = dict(item.get('service', {}))
                s_dict['hybrid_score'] = item.get('hybrid_score')
                s_dict['vector_score'] = item.get('vector_score')
                s_dict['lexical_score'] = item.get('lexical_score')
                s_dict['match_reasons'] = item.get('match_reasons')
                services_data.append(s_dict)

            return jsonify({
                'services': services_data,
                'total': len(services_data),
                'limit': limit,
                'offset': offset,
                'hybrid_search': True,
                'detected_assets': hybrid_res.get('detected_assets', []),
                'detected_symptoms': hybrid_res.get('detected_symptoms', [])
            }), 200
        except Exception as e:
            # En caso de excepción imprevista, continuar con fallback léxico SQL
            pass

    query = Service.query

    if search:
        query = query.filter(
            or_(
                Service.name.ilike(f'%{search}%'),
                Service.description.ilike(f'%{search}%')
            )
        )

    if category and category.lower() != 'todo' and category.lower() != 'todos':
        query = query.filter(Service.category.ilike(f'%{category}%'))

    services = query.order_by(Service.id.asc()).limit(limit).offset(offset).all()
    total = query.count()

    return jsonify({
        'services': [service.to_dict() for service in services],
        'total': total,
        'limit': limit,
        'offset': offset,
        'hybrid_search': False
    }), 200


@services_bp.route('/categories', methods=['GET'])
def list_categories():
    """List distinct categories available in services"""
    distinct_cats = db.session.query(Service.category).distinct().all()
    cats = [c[0] for c in distinct_cats if c[0]]
    if 'Todo' not in cats:
        cats.insert(0, 'Todo')
    return jsonify({
        'success': True,
        'categories': cats
    }), 200


@services_bp.route('/<int:service_id>', methods=['GET'])
def get_service(service_id):
    """Get single service by ID"""
    service = Service.query.get(service_id)

    if not service:
        return jsonify({'error': 'Service not found'}), 404

    return jsonify(service.to_dict()), 200
'''

with open(TARGET_PATH, "w", encoding="utf-8") as f:
    f.write(content.strip() + "\n")

print(f"[OK] services.py actualizado exitosamente en {TARGET_PATH}")
