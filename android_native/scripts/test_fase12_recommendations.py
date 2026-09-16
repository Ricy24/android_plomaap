import requests
import subprocess
import time

print("Generating token...")
result = subprocess.run([r"C:\Users\Anrid\plomaap_react_estable\backend_flask\venv\Scripts\python.exe", r"C:\Users\Anrid\Documents\plomaap\android_native\scripts\gen_token.py"], capture_output=True, text=True)
output = result.stdout.strip().split("\n")
if "TOKEN_GEN_SUCCESS" in output:
    token = output[-1]
    print("Token generated successfully.")
else:
    print("Failed to generate token:")
    print(result.stdout)
    print(result.stderr)
    exit(1)

base_url = "http://localhost:5000/api"
headers = {
    "Authorization": f"Bearer {token}"
}

print("\nFetching recommendations...")
rec_res = requests.get(f"{base_url}/recommendations", headers=headers)

if rec_res.status_code == 200:
    recommendations = rec_res.json()['data']
    for idx, rec in enumerate(recommendations):
        print(f"{idx+1}. {rec['name']} (Score: {rec['recommendation_score']})")
        print(f"   Razón: {rec['recommendation_reason']}")
        print(f"   ID Recomendación: {rec['recommendation_id']}")
        
    if recommendations:
        rec_id = recommendations[0]['recommendation_id']
        if rec_id:
            print(f"\nTracking click for recommendation ID {rec_id}...")
            click_res = requests.post(f"{base_url}/recommendations/{rec_id}/click", headers=headers)
            print(f"Click response: {click_res.status_code} - {click_res.text}")
            
            print(f"Tracking conversion for recommendation ID {rec_id}...")
            conv_res = requests.post(f"{base_url}/recommendations/{rec_id}/convert", headers=headers)
            print(f"Conversion response: {conv_res.status_code} - {conv_res.text}")
        else:
            print("\nNo recommendation ID to track.")
else:
    print(f"Failed to fetch recommendations: {rec_res.status_code} - {rec_res.text}")
