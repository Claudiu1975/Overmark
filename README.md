# Overmark

Overmark watches the speed of this device. While you stay over a limit you choose, a short beep marks every second. After 45 seconds an alarm sounds and the speed flashes red. Drop under the limit and the count starts over.

The limit starts at 90 km/h and moves in steps of 0.5 km/h. The app opens directly in the language of the device. The language list is not the first screen. A language you choose yourself stays on that device. Only the language and the speed limit are kept. Exit clears temporary files. No account, no analytics, nothing is sent.

If the screen is off and speed is over the limit, Overmark posts a notification. On Android that notification is visible on the lock screen. The phone keeps listening for GPS while tracking is armed.

Disclaimer and About are separate pages.

## What's new in 1.3.0

- Opens directly on the speed screen, in the language of this device. The language list is no longer the first screen.
- Disclaimer and About are separate pages.
- What's new is recorded for this version, and every published package stays in the archive.

The full archive is [CHANGELOG.md](CHANGELOG.md).

## About the author

Claudiu Florian Ivan is a truck driver with a passion for technology.

If Overmark helps you on the road, you can buy him a coffee: https://www.paypal.me/G0anga

## Open source

This is an open source project. Everyone is invited to contribute.

Repository: https://github.com/Claudiu1975/Overmark

License: [Apache-2.0](LICENSE).

## F-Droid

The Android app is prepared for F-Droid. Store text and the icon live in `fastlane/metadata/android/en-US/`. The metadata file to submit is [fdroid/com.overmark.app.yml](fdroid/com.overmark.app.yml). Tag `v1.4.0` is the build F-Droid should compile.

```bash
cd android
./gradlew assembleRelease
```

## Version archive

| Version | Package |
| --- | --- |
| 1.3.0 | [public/overmark-1.3.0.apk](public/overmark-1.3.0.apk) |
| 1.2.0 | [public/overmark-1.2.0.apk](public/overmark-1.2.0.apk) |
| 1.1.0 | [public/overmark-1.1.0.apk](public/overmark-1.1.0.apk) |

A newer build does not remove older packages.

## Try it

The web app includes a Bench mode so the timer can be heard without driving.

```bash
npm install
npm run dev
```

The current signed Android package is [public/overmark-1.3.0.apk](public/overmark-1.3.0.apk). It needs Android 8 or newer. Allow the browser to install it. Beeps use media volume. The alarm uses the alarm stream. Lock-screen alerts use the notification permission.

To rebuild the APK, install Android SDK platform 34 and build-tools 34.0.0, then run `android/build.sh`. The debug keystore is created on the first build. The script writes the new package and leaves every older package in place.
