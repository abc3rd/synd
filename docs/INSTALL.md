# Synd installation and testing

## Android
Open the repository in Android Studio (JDK 17, Android SDK 35) and run the app module, or run GitHub Actions > Android debug APK > Run workflow. After a successful run, download the synd-android-debug-apk artifact, unzip and install app-debug.apk on a test Android phone. This is a debug-signed development APK, not a Play Store release.
Android 12+ requires Bluetooth permissions. Enable Bluetooth and keep app in foreground.

## iPhone
Use Xcode on a Mac with Apple developer signing to compile the SwiftUI app project. Set a unique bundle ID and team, build to a real iPhone. TestFlight requires an Apple Developer Program account, App Store Connect app record, archive and upload. A source-only repository cannot provide an installable iPhone IPA without Apple signing.

## Release gates
No production release until verified real Android↔iPhone discovery, authenticated encryption, delivery/ACK, reliable voice messaging, privacy disclosures and tests. Push-to-talk and calls are NOT implemented by the discovery builds.
