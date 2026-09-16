import os
import requests
import json

ENV_PATH = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\.env"
gemini_key = None
with open(ENV_PATH, "r", encoding="utf-8") as f:
    for line in f:
        if line.startswith("GEMINI_API_KEY="):
            gemini_key = line.strip().split("=", 1)[1]
            break

models_to_test = ["gemini-3.6-flash", "gemini-3.5-flash-lite", "gemini-3-flash-preview"]

prompt = "Devuelve exclusivamente este JSON: {\"status\": \"ok\", \"test\": true}"

for m in models_to_test:
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{m}:generateContent?key={gemini_key}"
    payload = {
        "contents": [{
            "parts": [{"text": prompt}]
        }],
        "generationConfig": {
            "temperature": 0.1,
            "responseMimeType": "application/json"
        }
    }
    try:
        resp = requests.post(url, json=payload, timeout=20)
        print(f"Model {m}: status={resp.status_code}")
        if resp.status_code == 200:
            print("Response:", resp.json()["candidates"][0]["content"]["parts"][0]["text"])
            break
        else:
            print("Error:", resp.text[:200])
    except Exception as e:
        print(f"Model {m} Exception:", e)
