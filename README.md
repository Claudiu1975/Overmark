# Overmark

Overmark watches the speed of this device. While you stay over a limit you choose, a short beep marks every second. After 45 seconds an alarm sounds and the speed flashes red. Drop under the limit and the count starts over.

The limit starts at 90 km/h and moves in steps of 0.5 km/h. The app opens directly in the language of the device. The language list is not the first screen. A language you choose yourself stays on that device. Only the language and the speed limit are kept. Exit clears temporary files. No account, no analytics, nothing is sent.

If the screen is off and speed is over the limit, Overmark posts a notification. On Android that notification is visible on the lock screen. The phone keeps listening for GPS while tracking is armed.

Disclaimer and About are separate pages.

## What's new in 1.5.3

- A floating gauge shows the current speed on top of other apps.
- Settings offers two sizes, small and large.
- Float sits beside Minimize. Touching the gauge opens Overmark.

## What's new in 1.5.2

- Test: touching arm raises the simulated speed from 0, reaches the set limit in about 4 seconds and keeps going a little above it, so the alarm starts by itself.
- Test mode keeps running when the app is minimized, and the lock-screen alert appears when the speed is over the limit.
- Touching the lock-screen alert opens the app.

## What's new in 1.5.1

- Works on Android 7.0 and newer.

## What's new in 1.5.0

- Language, About and Disclaimer are a sub menu of Settings, and Settings is at the bottom of the screen.
- Bench is now called Test.
- European languages only.
- Contact: goanga.ro, admin@goanga.ro.

The full archive is [CHANGELOG.md](CHANGELOG.md).

## Contact

goanga.ro · admin@goanga.ro

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
| 1.5.3 | [public/overmark-1.5.3.apk](public/overmark-1.5.3.apk) |
| 1.5.2 | [public/overmark-1.5.2.apk](public/overmark-1.5.2.apk) |
| 1.5.0 | [public/overmark-1.5.0.apk](public/overmark-1.5.0.apk) |
| overmark-android-7 | [public/overmark-android-7.apk](public/overmark-android-7.apk) |
| 1.4.0 | [public/overmark-1.4.0.apk](public/overmark-1.4.0.apk) |
| 1.3.0 | [public/overmark-1.3.0.apk](public/overmark-1.3.0.apk) |
| 1.2.0 | [public/overmark-1.2.0.apk](public/overmark-1.2.0.apk) |
| 1.1.0 | [public/overmark-1.1.0.apk](public/overmark-1.1.0.apk) |

A newer build does not remove older packages.

## Try it

The web app includes a Test mode so the timer can be heard without driving.

```bash
npm install
npm run dev
```

The newest signed package in the archive is [public/overmark-1.5.3.apk](public/overmark-1.5.3.apk). It needs Android 7 or newer. Allow the browser to install it. Beeps use media volume. The alarm uses the alarm stream. Lock-screen alerts use the notification permission. The floating gauge needs permission to display over other apps.

To rebuild the APK, install Android SDK platform 34 and build-tools 34.0.0, then run `android/build.sh`. The debug keystore is created on the first build. The script writes the new package and leaves every older package in place.
