from pathlib import Path
import sys
from PIL import Image, ImageOps, ImageDraw

root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[1] / "tmp" / "sprint2_render"
pages = sorted(root.glob("page-*.png"))
for start in range(0, len(pages), 4):
    chosen = pages[start:start+4]
    thumbs = []
    for path in chosen:
        im = Image.open(path).convert("RGB")
        im.thumbnail((820, 1080))
        canvas = Image.new("RGB", (850, 1130), "white")
        canvas.paste(im, ((850-im.width)//2, 35))
        ImageDraw.Draw(canvas).text((20, 8), path.stem, fill="black")
        thumbs.append(canvas)
    sheet = Image.new("RGB", (1700, 2260), "#cfcfcf")
    for i, im in enumerate(thumbs):
        sheet.paste(im, ((i%2)*850, (i//2)*1130))
    sheet.save(root / f"contact-{start+1:02d}-{start+len(chosen):02d}.jpg", quality=88)
print(f"{len(pages)} pages; {((len(pages)+3)//4)} contact sheets")
