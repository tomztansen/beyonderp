import sqlite3, json, sys

DB = r'C:\Users\Lenovo\.local\share\mimocode\mimocode.db'
conn = sqlite3.connect(DB)
cur = conn.cursor()

for sid in sys.argv[1:]:
    print(f"\n=== Session: {sid} ===")
    cur.execute(
        """SELECT m.id, m.time_created, json_extract(m.data, '$.role') as role,
                  p.id as part_id, json_extract(p.data, '$.type') as part_type,
                  p.data as part_data
           FROM message m
           JOIN part p ON p.message_id = m.id
           WHERE m.session_id = ?
           ORDER BY m.time_created, p.time_created""",
        (sid,)
    )
    rows = cur.fetchall()
    for r in rows:
        msg_id, msg_time, role, part_id, part_type, part_data = r
        pd = json.loads(part_data) if part_data else {}
        if part_type == 'text':
            text = pd.get('text', '')[:1200]
            print(f"  [{role}] {text}")
        elif part_type == 'tool':
            tool_name = pd.get('tool', '?')
            state = pd.get('state', {})
            inp = str(state.get('input', ''))[:300]
            out = str(state.get('output', ''))[:500]
            print(f"  [{role}] TOOL:{tool_name} IN={inp} OUT={out[:300]}")
        elif part_type == 'checkpoint':
            print(f"  [{role}] CHECKPOINT event")
        elif part_type and 'step' in str(part_type):
            pass  # skip step markers
        else:
            preview = json.dumps(pd)[:300]
            print(f"  [{role}] {part_type}: {preview}")
    print()

conn.close()
