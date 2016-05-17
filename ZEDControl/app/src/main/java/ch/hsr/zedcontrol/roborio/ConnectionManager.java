package ch.hsr.zedcontrol.roborio;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import ch.hsr.zedcontrol.R;
import ch.hsr.zedcontrol.roborio.parsing.ParserData;
import ch.hsr.zedcontrol.roborio.parsing.RoboRIOStateParser;

/**
 * Handles the connection to the peripheral (roboRIO)
 */
public class ConnectionManager {
    public static final String ACTION_SERIAL_PORT_OPEN = ".ACTION_SERIAL_PORT_OPEN";

    public static final String ACTION_SERIAL_PORT_ERROR = ".ACTION_SERIAL_PORT_ERROR";
    public static final String EXTRA_SERIAL_PORT_ERROR = "/EXTRA_SERIAL_PORT_ERROR";

    public static final String ACTION_SERIAL_PORT_READ_LOCK = ".ACTION_SERIAL_PORT_READ_LOCK";
    public static final String EXTRA_SERIAL_PORT_READ_LOCK = "/EXTRA_SERIAL_PORT_READ_LOCK";

    public static final String ACTION_SERIAL_PORT_READ_MODE = ".ACTION_SERIAL_PORT_READ_MODE";
    public static final String EXTRA_SERIAL_PORT_READ_MODE = "/EXTRA_SERIAL_PORT_READ_MODE";

    public static final String ACTION_SERIAL_PORT_READ_BATTERY = ".ACTION_SERIAL_PORT_READ_BATTERY";
    public static final String EXTRA_SERIAL_PORT_READ_BATTERY = "/EXTRA_SERIAL_PORT_READ_BATTERY";

    private static final String ACTION_USB_PERMISSION = ".ACTION_USB_PERMISSION";

    private static final String TAG = ConnectionManager.class.getSimpleName();

    private static final int VENDOR_ID_FTDI_USB_TO_SERIAL_CABLE = 1027;

    private LocalBroadcastManager _localBroadcastManager;
    private UsbManager _usbManager;
    private UsbDevice _usbDevice;
    private UsbSerialDevice _serialPort;
    private RoboRIOModes _lastRequestedMode = null;
    private RoboRIOStateParser _parser = new RoboRIOStateParser();

    private final UsbSerialInterface.UsbReadCallback _usbReadCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                final String rawData = new String(arg0, "UTF-8");
                Log.i(TAG, "_usbReadCallback.onReceivedData: " + rawData);

                //TODO: let the RoboRIOStateParser return different objects for each case??
                final ArrayList<String> results = _parser.parse(rawData);
                final String keyWord = rawData.split(":")[0];

                for (String result : results) {
                    handleResult(result, keyWord);
                }

            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.toString());
            } catch (RoboRIOLockException | RoboRIOStateException | RoboRIOModeException e) {
                Log.e(TAG, e.toString());
                Intent intent = new Intent(ACTION_SERIAL_PORT_ERROR);
                intent.putExtra(EXTRA_SERIAL_PORT_ERROR, e.getMessage());
                _localBroadcastManager.sendBroadcast(intent);
            }
        }

        private void handleResult(String result, String keyWord) {
            switch (keyWord) {
                case "Lock":
                    broadcastLock(true);
                    break;

                case "Unlock":
                    broadcastLock(false);
                    break;

                case "Battery":
                    handleResultBattery(result);
                    break;

                case "State":
                    Log.d(TAG, "_usbReadCallback.handleResult() -> keep-alive signal for state: " + result);
                    break;

                case "Mode":
                    handleResultMode(result);
                    break;

                default:
                    Log.wtf(TAG, "_usbReadCallback.handleResult() -> Unhandled case: " + keyWord);
                    break;
            }
        }

        private void handleResultBattery(String result) {
            Intent voltageIntent = new Intent(ACTION_SERIAL_PORT_READ_BATTERY);
            voltageIntent.putExtra(EXTRA_SERIAL_PORT_READ_BATTERY, result);
            _localBroadcastManager.sendBroadcast(voltageIntent);
        }

        private void handleResultMode(String result) {
            if (_lastRequestedMode.equalsCommand(result)) {
                _lastRequestedMode = null;
            }

            Intent modeIntent = new Intent(ACTION_SERIAL_PORT_READ_MODE);
            modeIntent.putExtra(EXTRA_SERIAL_PORT_READ_MODE, result);
            _localBroadcastManager.sendBroadcast(modeIntent);
        }
    };

    private final BroadcastReceiver _usbActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_USB_PERMISSION:
                    Log.i(TAG, "_usbActionReceiver.onReceive() -> ACTION_USB_PERMISSION");
                    boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    handleActionUsbPermission(context, granted);
                    break;

                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    if (scanUsbDevicesForVendorId(context, VENDOR_ID_FTDI_USB_TO_SERIAL_CABLE)) {
                        Log.i(TAG, "_usbActionReceiver.onReceive() -> ACTION_USB_DEVICE_ATTACHED");
                    } else {
                        Log.w(TAG, "_usbActionReceiver.onReceive() -> ACTION_USB_DEVICE_ATTACHED -> unknown device");
                    }
                    _localBroadcastManager.sendBroadcast(intent);
                    break;

                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Log.i(TAG, "_usbActionReceiver.onReceive() -> ACTION_USB_DEVICE_DETACHED");
                    tryCloseSerialPort();
                    _localBroadcastManager.sendBroadcast(intent);
                    break;
            }
        }

        private void handleActionUsbPermission(Context context, boolean usbPermissionGranted) {
            Intent intent = new Intent(ACTION_SERIAL_PORT_ERROR);

            if (!usbPermissionGranted) {
                Log.w(TAG, "_usbActionReceiver.handleActionUsbPermission() -> Permission not granted.");
                intent.putExtra(EXTRA_SERIAL_PORT_ERROR, context.getString(R.string.error_permission_not_granted));
                _localBroadcastManager.sendBroadcast(intent);
                return;
            }

            UsbDeviceConnection connection = _usbManager.openDevice(_usbDevice);
            if (connection == null) {
                Log.e(TAG, "_usbActionReceiver.handleActionUsbPermission() -> _usbManager.openDevice() FAILED.");
                intent.putExtra(EXTRA_SERIAL_PORT_ERROR, context.getString(R.string.error_no_usb_connection));
                _localBroadcastManager.sendBroadcast(intent);
                return;
            }

            _serialPort = UsbSerialDevice.createUsbSerialDevice(_usbDevice, connection);
            if (_serialPort == null) {
                Log.e(TAG, "_usbActionReceiver.handleActionUsbPermission() -> _serialPort is null.");
                intent.putExtra(EXTRA_SERIAL_PORT_ERROR, context.getString(R.string.error_create_serialport));
                _localBroadcastManager.sendBroadcast(intent);
                return;
            }

            if (_serialPort.open()) {
                initSerialPort();
                Log.i(TAG, "_usbActionReceiver.handleActionUsbPermission() -> _serialPort open!");
                _localBroadcastManager.sendBroadcast(new Intent(ACTION_SERIAL_PORT_OPEN));
            } else {
                Log.e(TAG, "_usbActionReceiver.handleActionUsbPermission() -> _serialPort could not be opened.");
                intent.putExtra(EXTRA_SERIAL_PORT_ERROR, context.getString(R.string.error_open_serialport));
                _localBroadcastManager.sendBroadcast(intent);
            }
        }

        private void initSerialPort() {
            _serialPort.setBaudRate(38400);
            _serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
            _serialPort.setParity(UsbSerialInterface.PARITY_NONE);
            _serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
            _serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
            _serialPort.read(_usbReadCallback);
        }
    };


    /**
     * Constructor for ConnectionManager needs a Context to be able to use Android methods.
     *
     * @param context The Context object, usually an Activity.
     */
    public ConnectionManager(@NonNull Context context) {
        _usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        _localBroadcastManager = LocalBroadcastManager.getInstance(context);

        registerBroadcastReceiver(context);
    }

    private void broadcastLock(boolean hasLock) {
        Intent intent = new Intent(ACTION_SERIAL_PORT_READ_LOCK);
        intent.putExtra(EXTRA_SERIAL_PORT_READ_LOCK, hasLock);
        _localBroadcastManager.sendBroadcast(intent);
    }

    private void registerBroadcastReceiver(@NonNull Context context) {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        context.registerReceiver(_usbActionReceiver, filter);
    }


    /**
     * Scans all connected USB devices.
     * The first UsbDevice that matches the given vendorId will be requested for ACTION_USB_PERMISSION with a pending
     * intent.
     *
     * @param context  The Context object that requests the scan.
     * @param vendorId The vendorId that shall be looked for.
     * @return <c>true</c> if the vendorId could be matched to a connected UsbDevice - <c>false</c> otherwise.
     */
    public boolean scanUsbDevicesForVendorId(@NonNull Context context, int vendorId) {
        UsbDevice targetDevice = findConnectedUsbDeviceWithVendorId(vendorId);

        if (targetDevice == null) {
            return false;
        } else {
            _usbDevice = targetDevice;
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            _usbManager.requestPermission(targetDevice, pi);

            return true;
        }
    }


    @Nullable
    private UsbDevice findConnectedUsbDeviceWithVendorId(int vendorId) {
        HashMap<String, UsbDevice> usbDevices = _usbManager.getDeviceList();

        for (UsbDevice device : usbDevices.values()) {
            if (device.getVendorId() == vendorId) {
                return device;
            }

            Log.i(TAG, "Unknown vendorID: " + device.getVendorId());
            Log.i(TAG, "Unknown ManufacturerName: " + device.getManufacturerName());
            Log.i(TAG, "Unknown DeviceName: " + device.getDeviceName());
            Log.i(TAG, "Unknown ProductName: " + device.getProductName());
        }

        return null;
    }


    /**
     * Sends the command for the given mode to the open serial port. Only one mode-request can be sent at a time. The
     * ConnectionManager will ignore any further requests until the ACK has been received from
     * UsbSerialInterface.UsbReadCallback
     *
     * @param mode The requested mode
     */
    public void requestMode(RoboRIOModes mode) {
        if (_serialPort == null) {
            Log.w(TAG, "requestMode() -> _serialPort is null - ignore this call.");
            return;
        }

        // Getting a LOCK should always pass
        if (_lastRequestedMode != null && mode != RoboRIOModes.LOCK) {
            Log.w(TAG, "requestMode() -> still waiting for answer of last request: " + _lastRequestedMode);
            return;
        }

        _lastRequestedMode = mode;
        Log.i(TAG, "requestMode() -> writing to serial port: " + mode);
        _serialPort.write(mode.toString().getBytes());
    }


    /**
     * Closes all open connections and unregisters the receivers.
     *
     * @param context The Context object, ideally the same caller that instantiated this instance.
     */
    public void dispose(@NonNull Context context) {
        context.unregisterReceiver(_usbActionReceiver);
        tryCloseSerialPort();
    }


    private void tryCloseSerialPort() {
        try {
            _serialPort.close();
        } catch (NullPointerException e) {
            Log.w(TAG, "tryCloseSerialPort() -> catch NullPointerException, was the port open already?");
        }
    }

}
