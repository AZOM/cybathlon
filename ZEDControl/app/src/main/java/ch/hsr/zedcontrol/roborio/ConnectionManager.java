package ch.hsr.zedcontrol.roborio;

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
import java.util.HashMap;

import ch.hsr.zedcontrol.R;
import ch.hsr.zedcontrol.roborio.parsing.BatteryData;
import ch.hsr.zedcontrol.roborio.parsing.KeyWords;
import ch.hsr.zedcontrol.roborio.parsing.ParserData;
import ch.hsr.zedcontrol.roborio.parsing.RoboRIOParser;

/**
 * Handles the connection to the peripheral (roboRIO)
 */
public class ConnectionManager {
    public static final String ACTION_SERIAL_PORT_ERROR = ".ACTION_SERIAL_PORT_ERROR";
    public static final String EXTRA_SERIAL_PORT_ERROR = "/EXTRA_SERIAL_PORT_ERROR";

    public static final String ACTION_SERIAL_PORT_READ_LOCK = ".ACTION_SERIAL_PORT_READ_LOCK";
    public static final String EXTRA_SERIAL_PORT_READ_LOCK = "/EXTRA_SERIAL_PORT_READ_LOCK";

    public static final String ACTION_SERIAL_PORT_READ_MODE = ".ACTION_SERIAL_PORT_READ_MODE";
    public static final String EXTRA_SERIAL_PORT_READ_MODE = "/EXTRA_SERIAL_PORT_READ_MODE";

    public static final String ACTION_SERIAL_PORT_READ_BATTERY = ".ACTION_SERIAL_PORT_READ_BATTERY";
    public static final String EXTRA_SERIAL_PORT_READ_BATTERY = "/EXTRA_SERIAL_PORT_READ_BATTERY";

    public static final String ACTION_SERIAL_PORT_READ_STATE = ".ACTION_SERIAL_PORT_READ_STATE";
    public static final String EXTRA_SERIAL_PORT_READ_STATE = "/EXTRA_SERIAL_PORT_READ_STATE";

    private static final String TAG = ConnectionManager.class.getSimpleName();
    private static final int VENDOR_ID_FTDI = 1027;

    private LocalBroadcastManager _localBroadcastManager;
    private UsbManager _usbManager;
    private UsbDevice _usbDevice;
    private UsbSerialDevice _serialPort;
    private final BroadcastReceiver _usbActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Log.i(TAG, "_usbActionReceiver.onReceive() -> ACTION_USB_DEVICE_ATTACHED");
                    // since we use a device filter (Manifest) it will always be an expected UsbDevice here
                    _usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    openSerialPort(context);
                    break;

                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Log.i(TAG, "_usbActionReceiver.onReceive() -> ACTION_USB_DEVICE_DETACHED");
                    tryCloseSerialPort();
                    broadcastLockIntent(false);
                    break;
            }
        }
    };

    private RoboRIOParser _parser = new RoboRIOParser();

    private final UsbSerialInterface.UsbReadCallback _usbReadCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                final String rawData = new String(arg0, "UTF-8");

                for (ParserData parserData : _parser.parse(rawData)) {
                    handleResult(parserData);
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

        private void handleResult(ParserData parserData) {
            switch (parserData.getKeyWord()) {
                case LOCK:
                    handleResultLockUnlock(parserData);
                    break;

                case UNLOCK:
                    handleResultLockUnlock(parserData);
                    break;

                case MODE:
                    handleResultMode(parserData);
                    break;

                case BATTERY:
                    handleResultBattery((BatteryData) parserData);
                    break;

                case STATE:
                    handleResultState(parserData);
                    break;

                default:
                    Log.wtf(TAG, "_usbReadCallback.handleResult() -> Unhandled case: " + parserData.getKeyWord());
                    break;
            }
        }

        private void handleResultLockUnlock(ParserData parserData) {
            final boolean hasLock = parserData.getKeyWord() == KeyWords.LOCK;
            broadcastLockIntent(hasLock);
        }

        private void handleResultMode(ParserData parserData) {
            Intent modeIntent = new Intent(ACTION_SERIAL_PORT_READ_MODE);
            RoboRIOModes mode = RoboRIOModes.getModeFromStringDescription(parserData.getDescription());
            modeIntent.putExtra(EXTRA_SERIAL_PORT_READ_MODE, mode);
            _localBroadcastManager.sendBroadcast(modeIntent);
        }

        private void handleResultBattery(BatteryData batteryData) {
            Intent voltageIntent = new Intent(ACTION_SERIAL_PORT_READ_BATTERY);
            voltageIntent.putExtra(EXTRA_SERIAL_PORT_READ_BATTERY, batteryData.voltage);
            _localBroadcastManager.sendBroadcast(voltageIntent);
        }

        private void handleResultState(ParserData parserData) {
            Intent stateIntent = new Intent(ACTION_SERIAL_PORT_READ_STATE);
            RoboRIOModes mode = RoboRIOModes.getModeFromStringDescription(parserData.getDescription());
            stateIntent.putExtra(EXTRA_SERIAL_PORT_READ_STATE, mode);
            _localBroadcastManager.sendBroadcast(stateIntent);
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

        tryConnectedUsbDevice(context);
    }


    private void registerBroadcastReceiver(@NonNull Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        context.registerReceiver(_usbActionReceiver, filter);
    }


    private void tryConnectedUsbDevice(@NonNull Context context) {
        _usbDevice = findConnectedUsbDeviceWithVendorId(VENDOR_ID_FTDI);

        if (_usbDevice == null) {
            Log.i(TAG, "tryConnectedUsbDevice() -> Expected UsbDevice not attached to mobile phone - or power off...");
        } else {
            Log.i(TAG, "tryConnectedUsbDevice() -> found expected UsbDevice - going to open serial port...");
            openSerialPort(context);
        }
    }

    @Nullable
    private UsbDevice findConnectedUsbDeviceWithVendorId(int vendorId) {
        HashMap<String, UsbDevice> usbDevices = _usbManager.getDeviceList();

        for (UsbDevice device : usbDevices.values()) {
            if (device.getVendorId() == vendorId) {
                return device;
            }
            Log.w(TAG, "Unknown vendorID: " + device.getVendorId());
            Log.w(TAG, "Unknown ManufacturerName: " + device.getManufacturerName());
            Log.w(TAG, "Unknown DeviceName: " + device.getDeviceName());
            Log.w(TAG, "Unknown ProductName: " + device.getProductName());
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
            Log.w(TAG, "requestMode() -> _serialPort is null - assuming lock is lost.");
            broadcastLockIntent(false);
            return;
        }

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


    private void openSerialPort(Context context) {
        Intent intent = new Intent(ACTION_SERIAL_PORT_ERROR);

        UsbDeviceConnection connection = _usbManager.openDevice(_usbDevice);
        if (connection == null) {
            Log.e(TAG, "_usbActionReceiver.openSerialPort() -> _usbManager.openDevice() FAILED.");
            intent.putExtra(EXTRA_SERIAL_PORT_ERROR, context.getString(R.string.error_no_usb_connection));
            _localBroadcastManager.sendBroadcast(intent);
            return;
        }

        _serialPort = UsbSerialDevice.createUsbSerialDevice(_usbDevice, connection);
        if (_serialPort == null) {
            Log.e(TAG, "_usbActionReceiver.openSerialPort() -> _serialPort is null.");
            intent.putExtra(EXTRA_SERIAL_PORT_ERROR, context.getString(R.string.error_create_serialport));
            _localBroadcastManager.sendBroadcast(intent);
            return;
        }

        if (_serialPort.open()) {
            initSerialPort();
            Log.i(TAG, "_usbActionReceiver.handleActionUsbPermission() -> _serialPort open - requesting Lock");
            requestMode(RoboRIOModes.LOCK);
        } else {
            Log.e(TAG, "_usbActionReceiver.openSerialPort() -> _serialPort could not be opened.");
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


    private void broadcastLockIntent(boolean hasLock) {
        Intent intent = new Intent(ACTION_SERIAL_PORT_READ_LOCK);
        intent.putExtra(EXTRA_SERIAL_PORT_READ_LOCK, hasLock);
        _localBroadcastManager.sendBroadcast(intent);
    }


    private void tryCloseSerialPort() {
        try {
            _serialPort.close();
        } catch (NullPointerException e) {
            Log.w(TAG, "tryCloseSerialPort() -> catch NullPointerException, was the port open already?");
        }
    }

}
