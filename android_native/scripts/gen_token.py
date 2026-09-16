import sys
import os
from dotenv import load_dotenv

BACKEND_DIR = r"C:\Users\Anrid\plomaap_react_estable\backend_flask"
sys.path.insert(0, BACKEND_DIR)
os.chdir(BACKEND_DIR)
load_dotenv(os.path.join(BACKEND_DIR, ".env"), override=True)

from app import create_app
from app.utils.jwt_utils import generate_token

app = create_app('development')
with app.app_context():
    token = generate_token(1)
    print("TOKEN_GEN_SUCCESS")
    print(token)
