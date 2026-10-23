#!/bin/sh
set -eu
SDK="${ANDROID_HOME:-/tmp/android-sdk}"
BT="$SDK/build-tools/34.0.0"
PLATFORM="$SDK/platforms/android-34/android.jar"
ROOT="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
BUILD="$ROOT/build"
APP_ID="com.overmark.app"

if [ ! -f "$PLATFORM" ] || [ ! -x "$BT/aapt2" ]; then
  echo "Android SDK missing at $SDK" >&2
  exit 1
fi

rm -rf "$BUILD"
mkdir -p "$BUILD/flat" "$BUILD/classes" "$BUILD/dex" "$ROOT/res/drawable"

python3 - <<'PY'
import struct, zlib
w = h = 192
def px(x, y):
    cx = cy = 96
    dx, dy = x - cx + 0.5, y - cy + 0.5
    d = (dx*dx + dy*dy) ** 0.5
    bg = (18, 17, 14, 255)
    amber = (232, 163, 23, 255)
    red = (226, 59, 47, 255)
    if d > 88:
        return bg
    if 62 <= d <= 78:
        return amber
    # needle from center toward upper right
    # line from (96,96) to (132,62)
    nx, ny = 36, -34
    t = 0 if (nx*nx+ny*ny)==0 else max(0, min(1, (dx*nx + dy*ny) / (nx*nx+ny*ny)))
    lx, ly = nx*t, ny*t
    dist = ((dx-lx)**2 + (dy-ly)**2) ** 0.5
    if dist <= 5 and t > 0.05:
        return amber
    if d <= 7:
        return red
    return bg
raw = bytearray()
for y in range(h):
    raw.append(0)
    for x in range(w):
        raw.extend(px(x, y))
def chunk(tag, data):
    return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)) + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")
open("/workspace/android/res/drawable/ic_launcher.png", "wb").write(png)
PY

"$BT/aapt2" compile -o "$BUILD/flat" "$ROOT/res/drawable/ic_launcher.png"

"$BT/aapt2" link -o "$BUILD/unsigned.apk" \
  -I "$PLATFORM" \
  --manifest "$ROOT/AndroidManifest.xml" \
  --min-sdk-version 26 \
  --target-sdk-version 34 \
  -A "$ROOT/assets" \
  "$BUILD/flat/"*.flat

find "$ROOT/src" -name '*.java' > "$BUILD/sources.txt"
javac -encoding UTF-8 -source 8 -target 8 \
  -classpath "$PLATFORM" \
  -d "$BUILD/classes" \
  @"$BUILD/sources.txt"

"$BT/d8" --lib "$PLATFORM" --min-api 26 --output "$BUILD/dex" \
  $(find "$BUILD/classes" -name '*.class')

cp "$BUILD/unsigned.apk" "$BUILD/withdex.apk"
( cd "$BUILD/dex" && jar uf "$BUILD/withdex.apk" classes.dex )

"$BT/zipalign" -f 4 "$BUILD/withdex.apk" "$BUILD/aligned.apk"

KS="$ROOT/debug.keystore"
if [ ! -f "$KS" ]; then
  keytool -genkeypair -keystore "$KS" -storepass android -keypass android \
    -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Overmark, OU=Grok and you, O=Overmark, C=US"
fi

mkdir -p /workspace/public /workspace/artifacts
"$BT/apksigner" sign \
  --ks "$KS" --ks-key-alias androiddebugkey \
  --ks-pass pass:android --key-pass pass:android \
  --out /workspace/public/overmark-1.1.0.apk \
  "$BUILD/aligned.apk"
cp /workspace/public/overmark-1.1.0.apk /workspace/artifacts/Overmark-1.1.0.apk
"$BT/apksigner" verify --verbose /workspace/public/overmark-1.1.0.apk
"$BT/aapt" dump badging /workspace/public/overmark-1.1.0.apk | head -n 20
ls -lh /workspace/public/overmark-1.1.0.apk
rm -f /workspace/public/overmark-1.0.0.apk /workspace/artifacts/Overmark-1.0.0.apk
