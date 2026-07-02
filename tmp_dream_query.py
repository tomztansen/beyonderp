import sqlite3, json, sys

DB = r'C:\Users\Lenovo\.local\share\mimocode\mimocode.db'
conn = sqlite3.connect(DB)
cur = conn.cursor()

# Get user messages from key sessions
for sid in sys.argv[1:]:
    print(f"\n=== Session: {sid} ===")
    cur.execute(
        "SELECT id, session_id, time_created, data FROM message WHERE session_id = ? ORDER BY time_created",
        (sid,)
    )
    rows = cur.fetchall()
    for r in rows:
        d = json.loads(r[3])
        role = d.get('role', '?')
        content = d.get('content', '')
        if isinstance(content, list):
            texts = [c.get('text','') for c in content if isinstance(c, dict) and c.get('type')=='text']
            content = '\n'.join(texts)
        if len(content) > 1200:
            content = content[:1200] + '...[truncated]'
        print(f"  [{role}] {content}")
        print()

conn.close()
