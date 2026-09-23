import os
import sys
import time
from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler

APK_DIR = "/app/applet/.build-outputs"
PORT = 3000

HTML_PAGE = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HundredGram - Download APK & Source</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            background: linear-gradient(135deg, #0F172A 0%, #1E1B4B 50%, #0F172A 100%);
            color: #F8FAFC;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .container {
            background: rgba(30, 41, 59, 0.85);
            backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.12);
            border-radius: 24px;
            padding: 40px 32px;
            max-width: 480px;
            width: 100%;
            text-align: center;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
        }
        .app-icon {
            width: 88px;
            height: 88px;
            border-radius: 22px;
            background: linear-gradient(135deg, #EC4899 0%, #8B5CF6 50%, #3B82F6 100%);
            margin: 0 auto 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 42px;
            box-shadow: 0 10px 25px rgba(236, 72, 153, 0.4);
        }
        h1 {
            font-size: 28px;
            font-weight: 800;
            margin-bottom: 8px;
            background: linear-gradient(to right, #F472B6, #C084FC);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        p.subtitle {
            font-size: 14px;
            color: #94A3B8;
            margin-bottom: 28px;
            line-height: 1.5;
        }
        .badge-row {
            display: flex;
            justify-content: center;
            gap: 10px;
            margin-bottom: 28px;
        }
        .badge {
            background: rgba(255, 255, 255, 0.08);
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 12px;
            color: #CBD5E1;
            font-weight: 600;
        }
        .btn-download {
            display: block;
            width: 100%;
            padding: 16px 24px;
            margin-bottom: 14px;
            border-radius: 16px;
            text-decoration: none;
            font-weight: 700;
            font-size: 16px;
            transition: all 0.2s ease;
            box-shadow: 0 4px 14px rgba(0,0,0,0.2);
        }
        .btn-apk {
            background: linear-gradient(135deg, #EC4899 0%, #BE185D 100%);
            color: #FFFFFF;
        }
        .btn-apk:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(236, 72, 153, 0.5);
        }
        .btn-zip {
            background: rgba(255, 255, 255, 0.1);
            color: #F1F5F9;
            border: 1px solid rgba(255, 255, 255, 0.2);
        }
        .btn-zip:hover {
            background: rgba(255, 255, 255, 0.18);
            transform: translateY(-2px);
        }
        .install-guide {
            margin-top: 28px;
            background: rgba(15, 23, 42, 0.6);
            border-radius: 14px;
            padding: 16px;
            text-align: left;
            font-size: 12.5px;
            color: #94A3B8;
            line-height: 1.6;
        }
        .install-guide h3 {
            color: #E2E8F0;
            font-size: 13.5px;
            margin-bottom: 6px;
            font-weight: 700;
        }
        .install-guide ol {
            padding-left: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="app-icon">⚡</div>
        <h1>HundredGram</h1>
        <p class="subtitle">Next-Gen High Speed Social, Reels, Stories & Direct Messaging Android App</p>
        
        <div class="badge-row">
            <span class="badge">v1.0.0</span>
            <span class="badge">Android 8.0+</span>
            <span class="badge">Verified Ready</span>
        </div>

        <a href="/HundredGram.apk" download="HundredGram.apk" class="btn-download btn-apk">
            📥 Download APK (43 MB)
        </a>

        <a href="/HundredGram-Project.zip" download="HundredGram-Project.zip" class="btn-download btn-zip">
            📦 Download Full Project ZIP (28 MB)
        </a>

        <div class="install-guide">
            <h3>📱 Android Phone Installation:</h3>
            <ol>
                <li>Click <b>Download APK</b> button above.</li>
                <li>When download finishes, tap on <b>HundredGram.apk</b>.</li>
                <li>Allow <i>"Install Unknown Apps"</i> if prompted and tap <b>Install</b>.</li>
            </ol>
        </div>
    </div>
</body>
</html>
"""

class APKHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=APK_DIR, **kwargs)

    def do_HEAD(self):
        if self.path in ('/HundredGram-Project.zip', '/project.zip', '/download-zip', '/app.zip'):
            file_path = os.path.join(APK_DIR, 'HundredGram-Project.zip')
            if os.path.exists(file_path):
                self.send_response(200)
                self.send_header('Content-Type', 'application/zip')
                self.send_header('Content-Disposition', 'attachment; filename="HundredGram-Project.zip"')
                self.send_header('Content-Length', str(os.path.getsize(file_path)))
                self.end_headers()
                return
        if self.path in ('/download', '/HundredGram.apk', '/apk', '/HundredGram-latest.apk', '/app-debug.apk'):
            file_path = os.path.join(APK_DIR, 'HundredGram.apk')
            if not os.path.exists(file_path):
                file_path = os.path.join(APK_DIR, 'app-debug.apk')
            if os.path.exists(file_path):
                self.send_response(200)
                self.send_header('Content-Type', 'application/vnd.android.package-archive')
                self.send_header('Content-Disposition', 'attachment; filename="HundredGram.apk"')
                self.send_header('Content-Length', str(os.path.getsize(file_path)))
                self.end_headers()
            else:
                self.send_response(404)
                self.end_headers()
            return
        return super().do_HEAD()

    def do_GET(self):
        # Serve the download hub web page on root / or /index.html
        if self.path in ('/', '/index.html', '/home'):
            content = HTML_PAGE.encode('utf-8')
            self.send_response(200)
            self.send_header('Content-Type', 'text/html; charset=utf-8')
            self.send_header('Content-Length', str(len(content)))
            self.end_headers()
            self.wfile.write(content)
            return

        if self.path in ('/HundredGram-Project.zip', '/project.zip', '/download-zip', '/app.zip'):
            file_path = os.path.join(APK_DIR, 'HundredGram-Project.zip')
            if os.path.exists(file_path):
                self.send_response(200)
                self.send_header('Content-Type', 'application/zip')
                self.send_header('Content-Disposition', 'attachment; filename="HundredGram-Project.zip"')
                self.send_header('Content-Length', str(os.path.getsize(file_path)))
                self.end_headers()
                try:
                    with open(file_path, 'rb') as f:
                        while chunk := f.read(131072):
                            self.wfile.write(chunk)
                except Exception:
                    pass
            else:
                self.send_response(404)
                self.end_headers()
                self.wfile.write(b"Zip file not found")
            return

        if self.path in ('/download', '/HundredGram.apk', '/apk', '/HundredGram-latest.apk', '/app-debug.apk'):
            file_path = os.path.join(APK_DIR, 'HundredGram.apk')
            if not os.path.exists(file_path):
                file_path = os.path.join(APK_DIR, 'app-debug.apk')
            if os.path.exists(file_path):
                self.send_response(200)
                self.send_header('Content-Type', 'application/vnd.android.package-archive')
                self.send_header('Content-Disposition', 'attachment; filename="HundredGram.apk"')
                self.send_header('Content-Length', str(os.path.getsize(file_path)))
                self.end_headers()
                try:
                    with open(file_path, 'rb') as f:
                        while chunk := f.read(131072):
                            self.wfile.write(chunk)
                except Exception as e:
                    pass
            else:
                self.send_response(404)
                self.end_headers()
                self.wfile.write(b"APK not found")
            return

        return super().do_GET()

def run():
    if not os.path.exists(APK_DIR):
        os.makedirs(APK_DIR, exist_ok=True)
    os.chdir(APK_DIR)
    ThreadingHTTPServer.allow_reuse_address = True
    while True:
        try:
            httpd = ThreadingHTTPServer(('0.0.0.0', PORT), APKHandler)
            print(f"Serving APK & Download Hub on port {PORT}", flush=True)
            httpd.serve_forever()
        except Exception as e:
            print(f"Server error: {e}, restarting in 2s...", flush=True)
            time.sleep(2)

if __name__ == '__main__':
    run()
