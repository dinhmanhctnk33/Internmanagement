from pathlib import Path
import pypdfium2 as pdfium
from PIL import Image, ImageOps, ImageDraw

pdf_path = Path("outputs/sprint3/Sprint_3_Wireframe_Bo_Cuc_Giao_Dien.pdf")
out_dir = Path("outputs/sprint3/wireframe_layout_render")
out_dir.mkdir(parents=True, exist_ok=True)
pdf = pdfium.PdfDocument(str(pdf_path))
thumbs = []
for i, page in enumerate(pdf):
    image = page.render(scale=1.5).to_pil().convert("RGB")
    image.save(out_dir / f"page-{i+1}.png")
    thumb = image.copy(); thumb.thumbnail((350, 495))
    canvas = Image.new("RGB", (370, 530), "white")
    canvas.paste(thumb, ((370-thumb.width)//2, 20))
    ImageDraw.Draw(canvas).text((12, 505), f"Page {i+1}", fill="black")
    thumbs.append(canvas)
cols = 3
rows = (len(thumbs)+cols-1)//cols
sheet = Image.new("RGB", (cols*370, rows*530), "#D1D5DB")
for i, thumb in enumerate(thumbs):
    sheet.paste(thumb, ((i%cols)*370, (i//cols)*530))
sheet.save(out_dir / "contact-sheet.png")
print(f"pages={len(thumbs)}")
