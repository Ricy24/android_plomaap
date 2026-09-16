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
            # Find all sections with headings like '# 15.' or '## 15.' or 'Sección 15'
            matches = re.finditer(r'(#{1,3}\s*15[.\s][^\n\r]+)', content)
            for m in matches:
                start = m.start()
                print("HEADING:", m.group(0))
                print(content[start:start+2500])
                print("\n" + "="*50 + "\n")
            break
