import os

p = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\database_migration\MIGRATION_PLAN.md"
print("Existe:", os.path.exists(p))
print("Bytes:", os.path.getsize(p))
