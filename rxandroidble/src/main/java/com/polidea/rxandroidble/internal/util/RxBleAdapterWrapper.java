package com.polidea.rxandroidble.internal.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class RxBleAdapterWrapper {

    private final BluetoothAdapter bluetoothAdapter;

    @Inject
    public RxBleAdapterWrapper(@Nullable BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public BluetoothDevice getRemoteDevice(String macAddress) {
        return bluetoothAdapter.getRemoteDevice(macAddress);
    }

    public boolean hasBluetoothAdapter() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void startLeScan(List<ScanFilter> filters, ScanSettings settings, ScanCallback scanCallback) {
        BluetoothLeScannerCompat.getScanner().startScan(filters, settings, scanCallback);
    }

    public void stopLeScan(ScanCallback scanCallback) {
        BluetoothLeScannerCompat.getScanner().stopScan(scanCallback);
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }
}
