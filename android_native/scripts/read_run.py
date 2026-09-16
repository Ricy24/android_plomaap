import os
import sys

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
run_file = os.path.join(BACKEND_DIR, "run.py")

print("=== run.py ===")
with open(run_file, encoding="utf-8") as fp:
    print(fp.read())
