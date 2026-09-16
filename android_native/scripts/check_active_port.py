import os
from dotenv import load_dotenv

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
load_dotenv(os.path.join(BACKEND_DIR, ".env"))

print("POSTGRES_PORT in .env:", os.getenv("POSTGRES_PORT"))
print("DATABASE_URL in .env:", os.getenv("DATABASE_URL"))
print("DB_ENGINE in .env:", os.getenv("DB_ENGINE"))
