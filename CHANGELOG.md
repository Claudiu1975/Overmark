# What's new

Each version stays available. A newer build does not remove older packages.

## 1.5.5

- The floating ring now runs for 60 seconds. The first 45 are amber, the last 15 are red. The alarm still starts at 45.

Package: [public/overmark-1.5.5.apk](public/overmark-1.5.5.apk)

## 1.5.4

- The floating speed is larger. Over the limit it turns red and heavier, and the orange ring fills red one second at a time.
- At 45 seconds the speed on the gauge starts flashing.

Package: [public/overmark-1.5.4.apk](public/overmark-1.5.4.apk)

## 1.5.3

- A floating gauge shows the current speed on top of other apps.
- Settings offers two sizes, small and large. The size you pick is kept.
- Float sits beside Minimize. Touching the gauge opens Overmark.

Package: [public/overmark-1.5.3.apk](public/overmark-1.5.3.apk)

## 1.5.2

- Test: touching arm raises the simulated speed from 0, reaches the set limit in about 4 seconds and keeps going a little above it, so the alarm starts by itself.
- Test mode keeps running when the app is minimized, and the lock-screen alert appears when the speed is over the limit.
- Touching the lock-screen alert opens the app.

Package: [public/overmark-1.5.2.apk](public/overmark-1.5.2.apk)

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
