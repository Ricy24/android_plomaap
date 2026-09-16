import os
import sys

TARGET_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\services\embedding_provider.py"

content = '''"""
Embedding Provider Architecture for PlomApp
FASE 9: Embedding Provider (Secciones 16 y 48 del Plan Maestro)
Soporta:
  - VoyageEmbeddingProvider (modelo voyage-3-lite, dim 512)
  - DeterministicLocalEmbeddingProvider (fallback determinista offline, dim 512)
"""
import os
import math
import hashlib
import logging
from abc import ABC, abstractmethod
import time
import requests

logger = logging.getLogger(__name__)


class BaseEmbeddingProvider(ABC):
    """Interfaz base abstracta para proveedores de embeddings vectoriales."""
    
    @property
    @abstractmethod
    def model_name(self) -> str:
        pass

    @property
    @abstractmethod
    def dimension(self) -> int:
        pass

    @abstractmethod
    def embed_text(self, text: str) -> list:
        """Genera vector para una sola cadena de texto."""
        pass

    @abstractmethod
    def embed_batch(self, texts: list) -> list:
        """Genera vectores para una lista de cadenas de texto."""
        pass


class VoyageEmbeddingProvider(BaseEmbeddingProvider):
    """
    Proveedor oficial Voyage AI (https://api.voyageai.com/v1/embeddings).
    Modelo optimizado por defecto: voyage-3-lite (dimensión 512).
    """
    def __init__(self, api_key=None, model='voyage-3-lite'):
        self.api_key = api_key or os.getenv('VOYAGE_API_KEY', '')
        if not self.api_key:
            raise ValueError("VOYAGE_API_KEY no encontrada en variables de entorno")
        self._model = model
        self._dimension = 512
        self.api_url = 'https://api.voyageai.com/v1/embeddings'

    @property
    def model_name(self) -> str:
        return self._model

    @property
    def dimension(self) -> int:
        return self._dimension

    def embed_text(self, text: str) -> list:
        vectors = self.embed_batch([text])
        return vectors[0]

    def embed_batch(self, texts: list) -> list:
        if not texts:
            return []
        
        headers = {
            'Authorization': f'Bearer {self.api_key}',
            'Content-Type': 'application/json'
        }
        payload = {
            'input': [str(t).strip() for t in texts],
            'model': self._model
        }
        
        max_retries = 3
        for attempt in range(max_retries):
            try:
                resp = requests.post(self.api_url, headers=headers, json=payload, timeout=20)
                if resp.status_code == 429:
                    wait_sec = 21 * (attempt + 1)
                    logger.warning(f"Voyage AI rate limit (429). Esperando {wait_sec}s antes de reintentar (intento {attempt+1}/{max_retries})...")
                    time.sleep(wait_sec)
                    continue
                if resp.status_code != 200:
                    logger.error(f"Voyage AI API error {resp.status_code}: {resp.text}")
                    raise RuntimeError(f"Voyage API error: {resp.status_code} - {resp.text}")
                    
                data = resp.json()
                items = data.get('data', [])
                return [item['embedding'] for item in items]
            except requests.RequestException as e:
                if attempt == max_retries - 1:
                    logger.error(f"Fallo en llamada a Voyage AI tras {max_retries} intentos: {e}", exc_info=True)
                    raise e
                time.sleep(5)


class DeterministicLocalEmbeddingProvider(BaseEmbeddingProvider):
    """
    Proveedor determinista local basado en proyección de n-gramas y normalización L2.
    Garantiza vectores de 512 dimensiones consistentes, reproducibles y sin dependencia de red.
    """
    def __init__(self, dimension=512, model='local-hash-512'):
        self._dimension = dimension
        self._model = model

    @property
    def model_name(self) -> str:
        return self._model

    @property
    def dimension(self) -> int:
        return self._dimension

    def embed_text(self, text: str) -> list:
        raw_vec = [0.0] * self._dimension
        words = str(text).lower().split()
        if not words:
            return raw_vec
            
        for w in words:
            h = int(hashlib.sha256(w.encode('utf-8')).hexdigest(), 16)
            idx = h % self._dimension
            sign = 1.0 if ((h >> 8) & 1) == 1 else -1.0
            raw_vec[idx] += sign * (1.0 + math.log(len(w) + 1))
            
        # Normalización L2 (longitud euclidiana = 1.0)
        norm = math.sqrt(sum(x * x for x in raw_vec))
        if norm > 0:
            return [round(x / norm, 6) for x in raw_vec]
        return raw_vec

    def embed_batch(self, texts: list) -> list:
        return [self.embed_text(t) for t in texts]


def get_embedding_provider(provider_type=None) -> BaseEmbeddingProvider:
    """
    Fábrica que inicializa el proveedor configurado en backend.
    Prioriza Voyage AI si la clave está disponible; fallback a DeterministicLocal.
    """
    requested = provider_type or os.getenv('EMBEDDING_PROVIDER', 'voyage')
    voyage_key = os.getenv('VOYAGE_API_KEY')
    
    if requested.lower() == 'voyage' and voyage_key:
        try:
            return VoyageEmbeddingProvider(api_key=voyage_key)
        except Exception as e:
            logger.warning(f"No se pudo inicializar Voyage AI ({e}), conmutando a Local")
            
    # Proveedor local seguro
    return DeterministicLocalEmbeddingProvider()
'''

with open(TARGET_PATH, "w", encoding="utf-8") as f:
    f.write(content.strip() + "\n")

print(f"[OK] embedding_provider.py generado exitosamente en {TARGET_PATH}")
