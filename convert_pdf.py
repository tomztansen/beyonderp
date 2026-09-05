import markdown
from xhtml2pdf import pisa
import sys

def convert_md_to_pdf(md_file, pdf_file):
    # Read markdown
    with open(md_file, 'r', encoding='utf-8') as f:
        md_text = f.read()
    
    # Convert markdown to html
    # Use extensions for tables
    html_body = markdown.markdown(md_text, extensions=['tables'])
    
    # Wrap in basic HTML structure with simple styling
    html_content = f"""
    <html>
    <head>
    <meta charset="utf-8">
    <style>
        body {{
            font-family: Arial, sans-serif;
            font-size: 12px;
            line-height: 1.5;
        }}
        h1, h2, h3, h4 {{
            color: #333333;
        }}
        table {{
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }}
        th, td {{
            border: 1px solid #dddddd;
            padding: 8px;
            text-align: left;
        }}
        th {{
            background-color: #f2f2f2;
        }}
        @page {{
            size: a4;
            margin: 2cm;
            @frame footer_frame {{
                -pdf-frame-content: footer_content;
                left: 50pt; width: 512pt; top: 772pt; height: 20pt;
            }}
        }}
    </style>
    </head>
    <body>
        {html_body}
        <div id="footer_content" style="text-align: right;">Page <pdf:pagenumber></div>
    </body>
    </html>
    """
    
    # Generate PDF
    with open(pdf_file, "wb") as f_pdf:
        pisa_status = pisa.CreatePDF(html_content, dest=f_pdf)
    
    return pisa_status.err

if __name__ == "__main__":
    if convert_md_to_pdf("dokumentasi_proses_bisnis.md", "dokumentasi_proses_bisnis.pdf") == 0:
        print("Success")
    else:
        print("Error")
