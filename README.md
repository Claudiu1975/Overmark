# Overmark

GPS speed reminder. When the phone goes over a limit, each second is marked with a short beep. After 45 seconds an alarm sounds and the live speed flashes red. Drop under the limit and the count starts over.

The limit starts at 90 km/h and moves in steps of 0.5 km/h. The first launch asks for a language. Only the language and the speed limit are kept. Exit clears temporary files. No account, no analytics, nothing is sent.

Author: Claudiu Ivan. Version 1.1.0.

## Try it

The web app includes a Bench mode so the timer can be heard without driving.

```bash
npm install
npm run dev
```

The signed Android package is [public/overmark-1.1.0.apk](public/overmark-1.1.0.apk). It needs Android 8 or newer. Allow the browser to install it. Beeps use media volume. The alarm uses the alarm stream.

To rebuild the APK, install Android SDK platform 34 and build-tools 34.0.0, then run `android/build.sh`. The debug keystore is created on the first build.
