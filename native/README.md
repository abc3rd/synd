# Native BLE foundation (not a runnable application)
These Swift/Kotlin files are transport scaffolds for integrating into Xcode and Android Studio projects. No IPA/AAB is produced here.
Both advertise/scan the same GATT service. iOS supports central/peripheral roles; Android currently supports scan/advertise/GATT server only.
No real message exchange, audio, encryption or call signaling is implemented. Incoming writes are intentionally rejected.
Android: add runtime permissions and lifecycle management, BLE hardware feature checks, service registration sequencing, connection/client code.
iOS: add NSBluetoothAlwaysUsageDescription to Info.plist, background modes only if justified, lifecycle and user-visible permission UX.
Do not ship this as a messaging product until peer authentication, encryption and real-device testing are complete.
