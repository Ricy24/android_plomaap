import json

log_path = r"C:\Users\Anrid\.gemini\antigravity-ide\brain\7120e2ee-a7e2-4173-8a6c-30b2899b12b5\.system_generated\logs\transcript.jsonl"

with open(log_path, "r", encoding="utf-8") as f:
    for i, line in enumerate(f):
        if 2415 <= i <= 2422:
            data = json.loads(line)
            print(f"Line {i}: type={data.get('type')}")
            if data.get("tool_calls"):
                print("Tool calls:", data.get("tool_calls"))
