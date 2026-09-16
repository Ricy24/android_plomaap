import json
import sys
import re

sys.stdout.reconfigure(encoding='utf-8')

log_path = r"C:\Users\Anrid\.gemini\antigravity-ide\brain\7120e2ee-a7e2-4173-8a6c-30b2899b12b5\.system_generated\logs\transcript_full.jsonl"

with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        data = json.loads(line)
        if data.get('step_index') == 1513:
            content = data.get('content', '')
            parts = re.split(r'##\s*FASE\s*(\d+)', content)
            for i in range(1, len(parts), 2):
                p_num = int(parts[i])
                if p_num in [10, 11, 12]:
                    print(f"==================== FASE {p_num} ====================")
                    print(parts[i+1].strip())
                    print("\n")
            break
