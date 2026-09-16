import os

MIGRATION_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\database_migration"
BACKUP_DIR = os.path.join(MIGRATION_DIR, "backups")

files = os.listdir(BACKUP_DIR)
for f in files:
    if f.endswith("_manifest.txt"):
        print(f"=== {f} ===")
        with open(os.path.join(BACKUP_DIR, f), encoding="utf-8") as fp:
            print(fp.read())
