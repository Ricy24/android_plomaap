import os

TARGET = r"C:\Users\Anrid\plomaap_react_estable\backend_flask\app\services\event_service.py"

with open(TARGET, "r", encoding="utf-8") as f:
    code = f.read()

if "'NLU_QUERY_PROCESSED'" not in code:
    code = code.replace(
        "'SEARCH_PERFORMED',",
        "'SEARCH_PERFORMED',\n    'NLU_QUERY_PROCESSED',"
    )
    with open(TARGET, "w", encoding="utf-8") as f:
        f.write(code)
    print("[OK] NLU_QUERY_PROCESSED agregado a VALID_EVENT_TYPES")
else:
    print("[SKIP] Ya existía")
