import subprocess
import sys

print("--- REVISANDO DEPENDENCIAS DE POSTGRESQL EN VENV ---")
try:
    import psycopg2
    print("psycopg2 instalado:", psycopg2.__version__)
except ImportError:
    print("psycopg2 NO está instalado en el venv.")

try:
    import psycopg
    print("psycopg (v3) instalado:", psycopg.__version__)
except ImportError:
    print("psycopg (v3) NO está instalado en el venv.")

try:
    import asyncpg
    print("asyncpg instalado:", asyncpg.__version__)
except ImportError:
    print("asyncpg NO está instalado en el venv.")

try:
    import pgvector
    print("pgvector Python package instalado:", pgvector.__version__)
except ImportError:
    print("pgvector Python package NO está instalado en el venv.")
