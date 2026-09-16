import os

pgpass_path = os.path.expandvars(r"%APPDATA%\postgresql\pgpass.conf")
print("pgpass.conf exists?", os.path.exists(pgpass_path))
