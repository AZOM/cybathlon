package ch.hsr.zedcontrol.roborio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
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
import ch.hsr.zedcontrol.roborio.parsing.LockData;
import ch.hsr.zedcontrol.roborio.parsing.ModeData;
import ch.hsr.zedcontrol.roborio.parsing.ParserData;
import ch.hsr.zedcontrol.roborio.parsing.RoboRIOParser;
import ch.hsr.zedcontrol.roborio.parsing.StateData;

/**
 * Handles the connection to the peripheral (roboRIO)
 */
public class ConnectionManager {
    public static final String ACTION_SERIAL_PORT_ERROR = ".ACTION_SERIAL_PORT_ERROR";
    public static final String EXTRA_SERIAL_PORT_ERROR = "/EXTRA_SERIAL_PORT_ERROR";

    public static final String ACTION_SERIAL_PORT_READ_LOCK = ".ACTION_SERIAL_PORT_READ_LOCK";
    public static final String EXTRA_SERIAL_PORT_READ_LOCK = "/EXTRA_SERIAL_PORT_READ_LOCK";

    public static final String ACTION_SERIAL_PORT_READ_BATTERY = ".ACTION_SERIAL_PORT_READ_BATTERY";
    public static final String EXTRA_SERIAL_PORT_READ_BATTERY = "/EXTRA_SERIAL_PORT_READ_BATTERY";

    public static final String ACTION_SERIAL_PORT_READ_STATE = ".ACTION_SERIAL_PORT_READ_STATE";
    public static final String EXTRA_SERIAL_PORT_READ_STATE = "/EXTRA_SERIAL_PORT_READ_STATE";

    private static final String TAG = ConnectionManager.class.getSimpleName();
    private static final int CONNECTION_TIMEOUT_MS = 20000;
    private static final int VENDOR_ID_FTDI = 1027;

    private final LocalBroadcastManager _localBroadcastManager;
    private final RoboRIOParser _parser = new RoboRIOParser();

    private final UsbManager _usbManager;
    private UsbDevice _usbDevice;
    private UsbSerialDevice _serialPort;

    //TODO: check if this is really necessary and cannot be achieved by listening to the internal broadcasts properly
    private RoboRIOState _currentState = RoboRIOState.NO_MODE;

    private final Handler _handler = new Handler();
    private final Runnable _timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            Log.w(TAG, "_timeoutRunnable.run() -> timeout (" +
                    CONNECTION_TIMEOUT_MS + "ms) - sending broadcastLockIntent(false).");
            broadcastLockIntent(false);
        }
    };

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

    private final UsbSerialInterface.UsbReadCallback _usbReadCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                final String rawData = new String(arg0, "UTF-8");
                Log.d(TAG, "onReceivedData():   " + rawData);

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
                    handleResultLockUnlock((LockData) parserData);
                    break;

                case UNLOCK:
                    handleResultLockUnlock((LockData) parserData);
                    break;

                case MODE:
                    handleResultMode((ModeData) parserData);
                    break;

                case BATTERY:
                    handleResultBattery((BatteryData) parserData);
                    break;

                case STATE:
                    handleResultState((StateData) parserData);
                    break;

                default:
                    Log.wtf(TAG, "_usbReadCallback.handleResult() -> Unhandled case: " + parserData.getKeyWord());
                    break;
            }
        }

        private void handleResultLockUnlock(LockData lockData) {
            final boolean hasLock = lockData.getKeyWord() == KeyWords.LOCK;
            Log.i(TAG, "handleResultLockUnlock() -> hasLock: " + hasLock);
            broadcastLockIntent(hasLock);
        }

        private void handleResultMode(ModeData modeData) {
            // this only means that the requested mode has been understood - not yet active (see handleResultState)
            RoboRIOCommand command = RoboRIOCommand.getCommandFromStringDescription(modeData.getDescription());
            Log.i(TAG, "handleResultMode() -> Got ACK for requested command: " + command);
        }

        private void handleResultBattery(BatteryData batteryData) {
            Intent voltageIntent = new Intent(ACTION_SERIAL_PORT_READ_BATTERY);
            voltageIntent.putExtra(EXTRA_SERIAL_PORT_READ_BATTERY, batteryData.voltage);
            _localBroadcastManager.sendBroadcast(voltageIntent);
        }

        private void handleResultState(StateData stateData) {
            restartTimeoutHandler();

            RoboRIOState state = RoboRIOState.getStateFromStringDescription(stateData.getDescription());
            if (state == null) {
                Log.w(TAG, "handleResultState() -> IGNORING unhandled (intermediate) state: " + stateData);
            } else {
                Log.i(TAG, "handleResultState() -> keep-alive signal for state: " + state);
                Intent stateIntent = new Intent(ACTION_SERIAL_PORT_READ_STATE);
                stateIntent.putExtra(EXTRA_SERIAL_PORT_READ_STATE, state);
                _localBroadcastManager.sendBroadcast(stateIntent);

                _currentState = state;
            }
        }
    };


    private void restartTimeoutHandler() {
        _handler.removeCallbacks(_timeoutRunnable);
        _handler.postDelayed(_timeoutRunnable, CONNECTION_TIMEOUT_MS);
    }


    /**
     * Constructor for ConnectionManager needs a Context to be able to use Android methods.
     *
     * @param context The Context object, usually an Activity.
     */
    public ConnectionManager(@NonNull Context context) {
        _usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        _localBroadcastManager = LocalBroadcastManager.getInstance(context);

        registerBroadcastReceiver(context);

        initUsbSerialPort(context);
    }


    private void registerBroadcastReceiver(@NonNull Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        context.registerReceiver(_usbActionReceiver, filter);
    }


    /**
     * Establish a serial connection to the UsbDevice that is physically connected to the Android device if possible.
     *
     * @param context The Context object of the caller.
     */
    public void initUsbSerialPort(@NonNull Context context) {
        _usbDevice = findConnectedUsbDeviceWithVendorId(VENDOR_ID_FTDI);

        if (_usbDevice == null) {
            Log.w(TAG, "initUsbSerialPort() -> Expected UsbDevice not attached to mobile phone - or powered off?");
            broadcastLockIntent(false);
        } else {
            Log.i(TAG, "initUsbSerialPort() -> Found expected UsbDevice - going to open serial port...");
            openSerialPort(context);
            restartTimeoutHandler();
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
     * Returns the last known state of the RoboRIO.
     * @return Object of type {@link RoboRIOState}
     */
    public RoboRIOState getCurrentState() {
        return _currentState;
    }


    /**
     * Sends the command for the given command to the open serial port. This is an async call, if you want to be sure
     * that the command has been accepted by the RoboRIO, you have to check the answers from
     * UsbSerialInterface.UsbReadCallback.
     * {@see handleResultMode}: Handles ACKs for sent commands, this does not mean it has been executed yet.
     * {@see handleResultState}: Only here you will know, if the command has really been executed.
     *
     * @param command The requested command
     */
    public void sendCommand(RoboRIOCommand command) {
        if (_serialPort == null) {
            Log.w(TAG, "sendCommand() -> _serialPort is null - act as if lock is lost.");
            broadcastLockIntent(false);
        } else {
            Log.i(TAG, "sendCommand() -> " + command.name() + "(" + command + ")");
            _serialPort.write(command.toString().getBytes());
        }
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
            Log.e(TAG, "openSerialPort() -> _usbManager.openDevice() FAILED.");
            intent.putExtra(EXTRA_SERIAL_PORT_ERROR, context.getString(R.string.error_no_usb_connection));
            _localBroadcastManager.sendBroadcast(intent);
            return;
        }

        _serialPort = UsbSerialDevice.createUsbSerialDevice(_usbDevice, connection);
        if (_serialPort == null) {
            Log.e(TAG, "openSerialPort() -> _serialPort is null.");
            intent.putExtra(EXTRA_SERIAL_PORT_ERROR, context.getString(R.string.error_create_serialport));
            _localBroadcastManager.sendBroadcast(intent);
            return;
        }

        if (_serialPort.open()) {
            initSerialPort();
            Log.i(TAG, "openSerialPort() -> _serialPort open - requesting Lock");
            sendCommand(RoboRIOCommand.LOCK);
        } else {
            Log.e(TAG, "openSerialPort() -> _serialPort could not be opened.");
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
