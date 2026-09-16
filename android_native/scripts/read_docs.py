import os
import sys

# Ensure UTF-8 output
sys.stdout.reconfigure(encoding='utf-8')

DOCS = [
    r"C:\Users\Anrid\plomaap_react_estable\backend_flask\database_migration\MIGRATION_PLAN.md",
    r"C:\Users\Anrid\plomaap_react_estable\backend_flask\database_migration\ROLLBACK_GUIDE.md"
]

for doc in DOCS:
    print(f"==================================================")
    print(f"FILE: {doc}")
    print(f"==================================================")
    if os.path.exists(doc):
        with open(doc, encoding="utf-8", errors="replace") as fp:
            print(fp.read())
    else:
        print("FILE NOT FOUND!")
    print("\n")
