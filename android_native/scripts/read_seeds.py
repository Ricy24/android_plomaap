import os
import sys

# Ensure UTF-8 output
sys.stdout.reconfigure(encoding='utf-8')

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"

for s in ["seed.py", "seed_admin.py", "seed_technicians.py"]:
    p = os.path.join(BACKEND_DIR, s)
    if os.path.exists(p):
        print(f"\n=== {s} ===")
        with open(p, encoding="utf-8", errors="replace") as fp:
            for l in fp.readlines()[:45]:
                print(l.rstrip())
