import sys
sys.path.insert(0, r"C:\Users\Anrid\plomaap_react_estable\backend_flask")

from app import create_app
from app.database.models import Service, AssetTypeCatalog

app = create_app()
with app.app_context():
    print("=== SERVICES ===")
    for s in Service.query.all():
        print(f"ID {s.id}: {s.name} | Cat: {s.category} | Desc: {s.description}")

    print("\n=== CATALOG ASSETS ===")
    for a in AssetTypeCatalog.query.all():
        print(f"ID {a.id}: code={a.code} | name={a.name} | cat={a.category}")
