# SynD Native Build Plan
Status: architecture/reference protocol only; no functional native BLE application is claimed.

## Phase 1 — Real peer discovery
- iOS Swift CoreBluetooth CBCentralManager and CBPeripheralManager; privacy string NSBluetoothAlwaysUsageDescription.
- Android Kotlin BluetoothLeScanner, BluetoothLeAdvertiser and BluetoothGattServer; request BLUETOOTH_SCAN, BLUETOOTH_CONNECT, BLUETOOTH_ADVERTISE at runtime on Android 12+.
- Foreground-only discovery initially; test iPhone↔Android, iPhone↔iPhone and Android↔Android on physical devices.
- Derive device identity from app keys, not BLE MAC addresses.

## Phase 2 — Messaging
- Implement v0.1 framing in Swift and Kotlin and verify golden test vectors.
- Add pairing/identity verification, authenticated encryption, ACK/retry, bounded reassembly and persistence.
- Disable send UI until authenticated channel established.

## Phase 3 — Audio
- Record/play back compressed voice clips with AVAudioSession/AVAudioRecorder or AVAudioEngine on iOS and AudioRecord/MediaRecorder on Android.
- Implement half-duplex push-to-talk, jitter buffer, packet loss concealment and latency measurement.
- Prototype bidirectional live audio only after measured throughput supports it. BLE is not a guaranteed live voice transport.
- Respect mic permissions and visible recording indicators.

## Phase 4 — Relay, hardening, release
- TTL-based deduplicated store-and-forward relay; test iOS suspension/background limitations.
- Automated fuzzing and security review, power and reconnect testing.
- Android signed AAB via Android Studio/CI, internal Play testing.
- iOS signed IPA through macOS/Xcode with Apple developer signing and TestFlight.
- Physical hardware is required to verify BLE and audio. No store submission before tests.
