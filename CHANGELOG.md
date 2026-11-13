# What's new

Each version stays available. A newer build does not remove older packages.

## 1.7.3 Pro

- The Google Play package is now `com.overmark.pro`. This is a new paid Play app. The older `com.overmark.app` listing stays as it is and is not used for this release.
- Version 1.7.3 (versionCode 24). Basic stays at 1.7.1 (versionCode 20).

Compiled Pro is not published. Compile it from this source, or search the Google Play Store for Overmark Pro.

## 1.6.4 Pro

- Test no longer crashes when location permission is missing, denied, or revoked. The simulated speed still runs on screen. Staying alive after minimize still needs location permission, because that notice is a location foreground service.
- Arming GPS with location denied, or losing it mid-session, returns to standby instead of crashing. A denied notification permission no longer takes the process down with it.

Compiled Pro is not published. Compile it from this source, or search the Google Play Store for Overmark Pro.

## 1.6.3 Basic

- Same fix as Pro 1.6.4. Test runs without location permission and does not crash if that permission, or notifications, is denied or revoked.

The previous Basic package remains [public/overmark-basic-1.6.2.apk](public/overmark-basic-1.6.2.apk).

## 1.6.3 Pro

- The gauge size buttons on About work again. The choice is kept, and a gauge that is already showing changes size immediately.

Compiled Pro is not published. Compile it from this source, or search the Google Play Store for Overmark Pro.

## 1.6.2 Pro

- Same screen flow as Basic. Language is on the main screen. About holds the description, the navigation photo, the gauge size, and the legal text. Settings is gone, so language and About return to the main screen.

Pro is not published as a compiled package. Compile it from this source, or search the Google Play Store for Overmark Pro.

## 1.6.2 Basic

- Basic only. The empty Settings page is gone. Choosing a language, or leaving About, returns to the main screen.

Package: [public/overmark-basic-1.6.2.apk](public/overmark-basic-1.6.2.apk)

## 1.6.1 Basic

- Basic only. Pro stays at 1.6.0.
- Language moves to a small icon on the main screen.
- About now holds the Pro description, one landscape navigation photo with the gauge, and the legal text underneath.

Package: [public/overmark-basic-1.6.1.apk](public/overmark-basic-1.6.1.apk)

## 1.6.0

- Overmark is now two apps. Overmark Pro keeps the floating gauge. Overmark Basic does not.
- Basic shows screenshots of the Pro gauge and a description. The Google Play purchase link will be added later.
- Pro is the package prepared for Google Play as the paid app. It targets Android 16.

Package: [public/overmark-basic-1.6.0.apk](public/overmark-basic-1.6.0.apk). The Pro build from this version is not published.

## 1.5.1

- Runs on Android 7.0 and newer (it needed Android 8 before).
- On Android 7 the alert and the keep-alive notice use the older notification settings, and the vibration uses length only.

## 1.5.0

- Language, About and Disclaimer are no longer at the top of the screen. They are a sub menu of Settings, and Settings is at the bottom.
- Bench is now called Test.
- The language list keeps European languages only.
- The author, coffee and PayPal sections are gone. About shows goanga.ro and admin@goanga.ro.

## 1.4.0

- The Android app builds with Gradle, so F-Droid can compile it from this repository.
- Licensed under Apache-2.0.
- Unused network permissions are gone. The app still does not upload anything.

## 1.3.0

- Opens directly on the speed screen, in the language of this device. The language list is no longer the first screen.
- Disclaimer and About are separate pages.
- What's new is recorded for this version, and every published package stays in the archive.

Package: [public/overmark-1.3.0.apk](public/overmark-1.3.0.apk)

## 1.2.0

- About the author, a coffee invitation, and the PayPal code.
- Open source, with a link to the repository.
- A notification when speed is over the limit and the screen is off.
- The app can follow the device language. A language you choose stays on the device.

Package: [public/overmark-1.2.0.apk](public/overmark-1.2.0.apk)

## 1.1.0

- Translations, and a language you can change later.
- Disclaimer, and a statement that no data is collected.
- Exit and minimize. Exit clears temporary files. Only the language and the speed limit are kept.
- The limit starts at 90 km/h and moves in steps of 0.5 km/h.

Package: [public/overmark-1.1.0.apk](public/overmark-1.1.0.apk)
