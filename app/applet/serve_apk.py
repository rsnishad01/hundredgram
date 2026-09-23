import os
import sys
import time
import socket
import re
from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler
import socketserver

APK_DIR = "/app/applet/.build-outputs"
PORT = 3000

RANGE_REGEX = re.compile(r"bytes=(\d+)-(\d*)")

class ResilientServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    allow_reuse_address = True
    daemon_threads = True

    def handle_error(self, request, client_address):
        # Silently absorb all client aborts / socket timeouts / broken pipes
        pass


class ResilientAPKHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=APK_DIR, **kwargs)

    def log_message(self, format, *args):
        try:
            sys.stderr.write(f"[{time.strftime('%H:%M:%S')}] {self.address_string()} - {format % args}\n")
            sys.stderr.flush()
        except Exception:
            pass

    def end_headers(self):
        try:
            self.send_header('Access-Control-Allow-Origin', '*')
            self.send_header('Access-Control-Allow-Methods', 'GET, HEAD, OPTIONS')
            self.send_header('Access-Control-Allow-Headers', 'Range, Content-Type, Authorization')
            self.send_header('Accept-Ranges', 'bytes')
            super().end_headers()
        except Exception:
            pass

    def do_OPTIONS(self):
        try:
            self.send_response(200)
            self.end_headers()
        except Exception:
            pass

    def do_HEAD(self):
        self._serve_file(send_body=False)

    def do_GET(self):
        self._serve_file(send_body=True)

    def _serve_file(self, send_body=True):
        clean_path = self.path.split('?')[0].rstrip('/')
        
        # Resolve path
        if clean_path in ('', '/index.html'):
            target_file = os.path.join(APK_DIR, 'index.html')
            content_type = 'text/html; charset=utf-8'
            as_attachment = False
        elif clean_path in ('/HundredGram.apk', '/HundredGram-latest.apk', '/app-debug.apk', '/download', '/apk'):
            target_file = os.path.join(APK_DIR, 'HundredGram.apk')
            if not os.path.exists(target_file):
                target_file = os.path.join(APK_DIR, 'app-debug.apk')
            content_type = 'application/vnd.android.package-archive'
            as_attachment = True
        else:
            # Fallback to default SimpleHTTPRequestHandler for assets
            rel_path = clean_path.lstrip('/')
            target_file = os.path.join(APK_DIR, rel_path)
            content_type = self.guess_type(target_file)
            as_attachment = False

        if not os.path.exists(target_file) or not os.path.isfile(target_file):
            # If not found, fallback to index.html for root navigation
            target_file = os.path.join(APK_DIR, 'index.html')
            content_type = 'text/html; charset=utf-8'
            as_attachment = False

        try:
            file_size = os.path.getsize(target_file)
        except Exception:
            try:
                self.send_error(404, "File not found")
            except Exception:
                pass
            return

        range_header = self.headers.get('Range')
        start = 0
        end = file_size - 1

        if range_header:
            match = RANGE_REGEX.match(range_header.strip())
            if match:
                s_str, e_str = match.groups()
                start = int(s_str)
                if e_str:
                    end = int(e_str)
                if start >= file_size or end >= file_size or start > end:
                    try:
                        self.send_response(416) # Range Not Satisfiable
                        self.send_header('Content-Range', f'bytes */{file_size}')
                        self.end_headers()
                    except Exception:
                        pass
                    return

        content_length = end - start + 1

        try:
            if range_header and (start > 0 or end < file_size - 1):
                self.send_response(206) # Partial Content
                self.send_header('Content-Range', f'bytes {start}-{end}/{file_size}')
            else:
                self.send_response(200)

            self.send_header('Content-Type', content_type)
            self.send_header('Content-Length', str(content_length))
            if as_attachment:
                self.send_header('Content-Disposition', 'attachment; filename="HundredGram.apk"')
            self.end_headers()

            if send_body:
                with open(target_file, 'rb') as f:
                    if start > 0:
                        f.seek(start)
                    remaining = content_length
                    chunk_size = 131072
                    while remaining > 0:
                        to_read = min(remaining, chunk_size)
                        buf = f.read(to_read)
                        if not buf:
                            break
                        self.wfile.write(buf)
                        remaining -= len(buf)
        except (BrokenPipeError, ConnectionResetError, ConnectionAbortedError, socket.error):
            # Client closed stream or cancelled download
            pass
        except Exception as e:
            try:
                sys.stderr.write(f"Transfer exception: {e}\n")
            except Exception:
                pass


def run():
    os.chdir(APK_DIR)
    while True:
        try:
            with ResilientServer(('0.0.0.0', PORT), ResilientAPKHandler) as httpd:
                print(f"HundredGram Resilient Server listening on port {PORT}...", flush=True)
                httpd.serve_forever()
        except KeyboardInterrupt:
            break
        except Exception as e:
            sys.stderr.write(f"Server loop error: {e}, restarting immediately...\n")
            time.sleep(0.5)


if __name__ == '__main__':
    run()
