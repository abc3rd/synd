import SwiftUI

struct SyndHomeView: View {
    @StateObject private var bluetooth = SyndBLETransport()

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 20) {
                Text("SYND • BLUETOOTH")
                    .font(.largeTitle.bold())
                Text("Discovery build — messaging and voice are not yet enabled.")
                    .foregroundStyle(.secondary)
                Button(bluetooth.isScanning ? "Stop scanning" : "Find nearby Synd phones") {
                    if bluetooth.isScanning { bluetooth.stopScanning() }
                    else { bluetooth.startScanning() }
                }
                .buttonStyle(.borderedProminent)
                if let error = bluetooth.lastError {
                    Text(error).foregroundStyle(.red)
                }
                Text("Nearby devices").font(.headline)
                List(Array(bluetooth.nearby.keys), id: \.self) { id in
                    HStack {
                        Text(bluetooth.nearby[id] ?? "Nearby device")
                        Spacer()
                        Button("Connect") { bluetooth.connect(to: id) }
                    }
                }
                Text("Foreground Bluetooth discovery only. No user messages or audio are transmitted.")
                    .font(.footnote).foregroundStyle(.secondary)
            }
            .padding()
        }
    }
}
