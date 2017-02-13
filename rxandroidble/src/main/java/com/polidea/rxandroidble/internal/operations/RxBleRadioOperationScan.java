package com.polidea.rxandroidble.internal.operations;

import android.os.DeadObjectException;
import com.polidea.rxandroidble.exceptions.BleException;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.internal.RxBleInternalScanResult;
import com.polidea.rxandroidble.internal.RxBleLog;
import com.polidea.rxandroidble.internal.RxBleRadioOperation;
import com.polidea.rxandroidble.internal.util.RxBleAdapterWrapper;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import java.util.List;

public class RxBleRadioOperationScan extends RxBleRadioOperation<RxBleInternalScanResult> {

    private final RxBleAdapterWrapper rxBleAdapterWrapper;
    private final ScanSettings scanSettings;
    private final List<ScanFilter> filters;
    private volatile boolean isStarted = false;
    private volatile boolean isStopped = false;

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                RxBleRadioOperationScan.this.onNext(new RxBleInternalScanResult(result.getDevice(), result.getRssi(),
                        result.getScanRecord().getBytes()));
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            RxBleRadioOperationScan.this.onError(new BleScanException(BleScanException.BLUETOOTH_CANNOT_START));
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            RxBleRadioOperationScan.this.onNext(new RxBleInternalScanResult(result.getDevice(), result.getRssi(),
                    result.getScanRecord().getBytes()));
        }
    };

    public RxBleRadioOperationScan(RxBleAdapterWrapper rxBleAdapterWrapper, ScanSettings scanSettings,
                                   List<ScanFilter> filters) {
        this.rxBleAdapterWrapper = rxBleAdapterWrapper;
        this.scanSettings = scanSettings;
        this.filters = filters;
    }

    @Override
    protected void protectedRun() {

        try {
            rxBleAdapterWrapper.startLeScan(filters, scanSettings, scanCallback);

            synchronized (this) { // synchronization added for stopping the scan
                isStarted = true;
                if (isStopped) {
                    stop();
                }
            }

        } catch (Throwable throwable) {
            isStarted = true;
            RxBleLog.e(throwable, "Error while calling BluetoothAdapter.startLeScan()");
            onError(new BleScanException(BleScanException.BLUETOOTH_CANNOT_START));
        } finally {
            releaseRadio();
        }
    }

    // synchronized keyword added to be sure that operation will be stopped no matter which thread will call it
    public synchronized void stop() {
        isStopped = true;
        if (isStarted) {
            // TODO: [PU] 29.01.2016 https://code.google.com/p/android/issues/detail?id=160503
            rxBleAdapterWrapper.stopLeScan(scanCallback);
        }
    }

    @Override
    protected BleException provideException(DeadObjectException deadObjectException) {
        return new BleScanException(BleScanException.BLUETOOTH_DISABLED, deadObjectException);
    }
}
