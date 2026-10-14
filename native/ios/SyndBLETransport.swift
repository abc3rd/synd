import Foundation
import CoreBluetooth

/// Foreground BLE transport foundation. This class does not authenticate peers or send user content.
final class SyndBLETransport: NSObject, ObservableObject {
    static let serviceID = CBUUID(string: "7c31a4e0-8d62-4f1d-a9b3-104c8a63d001")
    static let rxID = CBUUID(string: "7c31a4e0-8d62-4f1d-a9b3-104c8a63d002")
    static let txID = CBUUID(string: "7c31a4e0-8d62-4f1d-a9b3-104c8a63d003")

    @Published private(set) var nearby: [UUID: String] = [:]
    @Published private(set) var isScanning = false
    @Published private(set) var lastError: String?
    private var central: CBCentralManager!
    private var peripheralManager: CBPeripheralManager!
    private var receiveCharacteristic: CBMutableCharacteristic?
    private var transmitCharacteristic: CBMutableCharacteristic?
    private var discovered: [UUID: CBPeripheral] = [:]
    private var connected: [UUID: CBPeripheral] = [:]

    override init() {
        super.init()
        central = CBCentralManager(delegate: self, queue: nil)
        peripheralManager = CBPeripheralManager(delegate: self, queue: nil)
    }

    func startScanning() {
        guard central.state == .poweredOn else {
            lastError = "Bluetooth is not ready"
            return
        }
        central.scanForPeripherals(withServices: [Self.serviceID],
                                   options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
        isScanning = true
    }

    func stopScanning() {
        central.stopScan()
        isScanning = false
    }

    func connect(to identifier: UUID) {
        guard let peer = discovered[identifier] else { return }
        central.connect(peer, options: nil)
    }
}

extension SyndBLETransport: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state != .poweredOn { stopScanning() }
    }

    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral,
                        advertisementData: [String: Any], rssi RSSI: NSNumber) {
        discovered[peripheral.identifier] = peripheral
        nearby[peripheral.identifier] = peripheral.name ?? "Nearby Synd device"
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        connected[peripheral.identifier] = peripheral
        peripheral.delegate = self
        peripheral.discoverServices([Self.serviceID])
    }

    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        lastError = error?.localizedDescription ?? "Connection failed"
    }

    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        connected.removeValue(forKey: peripheral.identifier)
        if let error = error { lastError = error.localizedDescription }
    }
}

extension SyndBLETransport: CBPeripheralDelegate {
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let error = error { lastError = error.localizedDescription; return }
        peripheral.services?.filter { $0.uuid == Self.serviceID }.forEach {
            peripheral.discoverCharacteristics([Self.rxID, Self.txID], for: $0)
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if let error = error { lastError = error.localizedDescription; return }
        service.characteristics?.filter { $0.uuid == Self.txID && $0.properties.contains(.notify) }
            .forEach { peripheral.setNotifyValue(true, for: $0) }
    }
}

extension SyndBLETransport: CBPeripheralManagerDelegate {
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        guard peripheral.state == .poweredOn else { return }
        let rx = CBMutableCharacteristic(type: Self.rxID, properties: [.write],
                                         value: nil, permissions: [.writeable])
        let tx = CBMutableCharacteristic(type: Self.txID, properties: [.notify],
                                         value: nil, permissions: [])
        receiveCharacteristic = rx
        transmitCharacteristic = tx
        peripheral.add(CBMutableService(type: Self.serviceID, primary: true).configured(rx: rx, tx: tx))
    }

    func peripheralManager(_ peripheral: CBPeripheralManager, didAdd service: CBService, error: Error?) {
        if let error = error { lastError = error.localizedDescription; return }
        peripheral.startAdvertising([CBAdvertisementDataServiceUUIDsKey: [Self.serviceID],
                                     CBAdvertisementDataLocalNameKey: "Synd"])
    }

    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveWrite requests: [CBATTRequest]) {
        for request in requests {
            // Intentionally reject incoming application data until authenticated messaging exists.
            peripheral.respond(to: request, withResult: .writeNotPermitted)
        }
    }
}

private extension CBMutableService {
    func configured(rx: CBMutableCharacteristic, tx: CBMutableCharacteristic) -> CBMutableService {
        characteristics = [rx, tx]
        return self
    }
}
