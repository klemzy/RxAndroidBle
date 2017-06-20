package com.polidea.rxandroidble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.polidea.rxandroidble.internal.util.RxBleAdapterWrapper
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanSettings

class MockRxBleAdapterWrapper extends RxBleAdapterWrapper {

    static class ScanData {
        BluetoothDevice device
        int rssi
        byte[] scanRecord

        ScanData(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            this.device = bluetoothDevice
            this.rssi = rssi
            this.scanRecord = scanRecord
        }
    }

    private List<ScanData> scanDataList = new ArrayList<>()
    private Set<BluetoothDevice> bondedDevices = new HashSet<>()

    MockRxBleAdapterWrapper() {
        super(null)
    }

    MockRxBleAdapterWrapper(BluetoothAdapter bluetoothAdapter) {
        super(bluetoothAdapter)
    }

    def addScanResult(BluetoothDevice bluetoothDevice, int rssi, byte[] scanResult) {
        scanDataList.add(new ScanData(bluetoothDevice, rssi, scanResult))
    }

    def addBondedDevice(BluetoothDevice bluetoothDevice) {
        bondedDevices.add(bluetoothDevice)
    }

    @Override
    BluetoothDevice getRemoteDevice(String macAddress) {
        scanDataList.find {
            it.device.getAddress() == macAddress
        }?.device
    }

    @Override
    void startLeScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback scanCallback) {
        super.startLeScan(filters, settings, scanCallback)
        scanDataList.each {
            callback.onLeScan(it.device, it.rssi, it.scanRecord)
        }
    }

    @Override
    void stopLeScan(ScanCallback scanCallback) {
        super.stopLeScan(scanCallback)
    }
    
    @Override
    boolean hasBluetoothAdapter() {
        return true
    }

    @Override
    boolean isBluetoothEnabled() {
        return true
    }

    @Override
    Set<BluetoothDevice> getBondedDevices() {
        return bondedDevices
    }
}
