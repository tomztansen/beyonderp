import sqlite3, json, sys

DB = r'C:\Users\Lenovo\.local\share\mimocode\mimocode.db'
conn = sqlite3.connect(DB)
cur = conn.cursor()

# Get messages from key sessions with full structure inspection
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
        # Check all keys in data
        keys = list(d.keys())
        content = d.get('content', '')
        text_val = d.get('text', '')
        # Try different content formats
        if isinstance(content, list):
            texts = [c.get('text','') for c in content if isinstance(c, dict) and c.get('type')=='text']
            content = '\n'.join(texts)
        if not content and text_val:
            content = text_val
        if not content:
            # Show raw keys and first 500 chars of data
            content = f"[keys: {keys}] {json.dumps(d)[:500]}"
        if len(content) > 1000:
            content = content[:1000] + '...[truncated]'
        print(f"  [{role}] {content}")
        print()

conn.close()
