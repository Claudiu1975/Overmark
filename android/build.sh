#!/bin/sh
set -eu
SDK="${ANDROID_HOME:-/tmp/android-sdk}"
BT="$SDK/build-tools/34.0.0"
PLATFORM="$SDK/platforms/android-36/android.jar"
ROOT="$(CDPATH= cd -- "$(dirname "$0")" && pwd)"
BUILD="$ROOT/build"
EDITION="${1:-pro}"
if [ "$EDITION" = "basic" ]; then
  PRO_VALUE=false
  APP_ID="com.overmark.basic"
  LABEL="Overmark"
else
  EDITION="pro"
  PRO_VALUE=true
  APP_ID="com.overmark.app"
  LABEL="Overmark Pro"
fi
APP_ID_OUT="$APP_ID"

if [ ! -f "$PLATFORM" ] || [ ! -x "$BT/aapt2" ]; then
  echo "Android SDK missing at $SDK" >&2
  exit 1
fi

rm -rf "$BUILD"
mkdir -p "$BUILD/flat" "$BUILD/classes" "$BUILD/dex" "$ROOT/res/drawable"
export ICON_OUT="$ROOT/res/drawable/ic_launcher.png"

python3 - <<'PY'
import os, struct, zlib
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
open(os.environ["ICON_OUT"], "wb").write(png)
PY

if [ -f "$ROOT/../src/lib/overmark/releases.json" ]; then
  cp "$ROOT/../src/lib/overmark/releases.json" "$ROOT/assets/releases.json"
fi

"$BT/aapt2" compile -o "$BUILD/flat" "$ROOT/res/drawable/ic_launcher.png"
mkdir -p "$BUILD/res/values" "$BUILD/gen/com/overmark/app"
printf '%s\n' '<?xml version="1.0" encoding="utf-8"?>' '<resources>' \
  "    <string name=\"app_name\">$LABEL</string>" '</resources>' > "$BUILD/res/values/strings.xml"
"$BT/aapt2" compile -o "$BUILD/flat" "$BUILD/res/values/strings.xml"
cat > "$BUILD/gen/com/overmark/app/BuildConfig.java" << EOF
package com.overmark.app;
public final class BuildConfig {
    public static final boolean DEBUG = false;
    public static final String APPLICATION_ID = "$APP_ID";
    public static final String BUILD_TYPE = "release";
    public static final String FLAVOR = "$EDITION";
    public static final int VERSION_CODE = 14;
    public static final String VERSION_NAME = "1.6.0";
    public static final boolean PRO = $PRO_VALUE;
}
EOF

MANIFEST="$ROOT/AndroidManifest.xml"
if [ "$EDITION" = "basic" ]; then
  sed '/SYSTEM_ALERT_WINDOW/d' "$ROOT/AndroidManifest.xml" > "$BUILD/AndroidManifest.xml"
  MANIFEST="$BUILD/AndroidManifest.xml"
fi

"$BT/aapt2" link -o "$BUILD/unsigned.apk" \
  -I "$PLATFORM" \
  --manifest "$MANIFEST" \
  --rename-manifest-package "$APP_ID" \
  --min-sdk-version 24 \
  --target-sdk-version 36 \
  -A "$ROOT/assets" \
  "$BUILD/flat/"*.flat

find "$ROOT/src" -name '*.java' > "$BUILD/sources.txt"
echo "$BUILD/gen/com/overmark/app/BuildConfig.java" >> "$BUILD/sources.txt"
javac -encoding UTF-8 -source 8 -target 8 \
  -classpath "$PLATFORM" \
  -d "$BUILD/classes" \
  @"$BUILD/sources.txt"

"$BT/d8" --lib "$PLATFORM" --min-api 24 --output "$BUILD/dex" \
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

mkdir -p "$ROOT/../public" "$ROOT/../artifacts"
"$BT/apksigner" sign \
  --ks "$KS" --ks-key-alias androiddebugkey \
  --ks-pass pass:android --key-pass pass:android \
  --out "$ROOT/../public/overmark-$EDITION-1.6.0.apk" \
  "$BUILD/aligned.apk"
cp "$ROOT/../public/overmark-$EDITION-1.6.0.apk" "$ROOT/../artifacts/Overmark-$EDITION-1.6.0.apk" 2>/dev/null || true
"$BT/apksigner" verify --verbose "$ROOT/../public/overmark-$EDITION-1.6.0.apk"
"$BT/aapt" dump badging "$ROOT/../public/overmark-$EDITION-1.6.0.apk" | head -n 12
ls -lh "$ROOT/../public/overmark-$EDITION-1.6.0.apk"
