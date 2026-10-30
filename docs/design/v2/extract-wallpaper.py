"""Extract only the clean background embedded in the visual prototype."""
from pathlib import Path
import base64
import re

root = Path(__file__).resolve().parent
html = (root / 'index.html').read_text()
match = re.search(r"data:image/jpeg;base64,([^']+)", html)
if match is None:
    raise SystemExit('No embedded JPEG wallpaper found')
output = root / 'wallpaper.jpg'
output.write_bytes(base64.b64decode(match.group(1), validate=True))
print(f'Extracted clean wallpaper to {output.name}')
