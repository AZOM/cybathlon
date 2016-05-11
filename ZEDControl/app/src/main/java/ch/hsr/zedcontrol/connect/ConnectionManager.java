package ch.hsr.zedcontrol.connect;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Handles the connection to the peripheral (roboRIO)
 */
public class ConnectionManager {
    public static final String ACTION_USB_PERMISSION = ".ACTION_USB_PERMISSION";
    public static final String EXTRA_USB_PERMISSION_ERROR = "/EXTRA_USB_PERMISSION_ERROR";
    public static final String EXTRA_USB_PERMISSION_SUCCESS = "/EXTRA_USB_PERMISSION_SUCCESS";

    private static String TAG = ConnectionManager.class.getSimpleName();

    private static final int VENDOR_ID_FTDI_USB_TO_SERIAL_CABLE = 1027;

    private UsbManager _usbManager;
    private UsbDevice _usbDevice;
    private UsbSerialDevice _serialPort;

    //TODO: check if it is possible to get rid of storing Context as a field (always pass as parameter)
    private Context _context;

    private final BroadcastReceiver _usbActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_USB_PERMISSION:
                    Log.i(TAG, "_usbActionReceiver.onReceive() -> ACTION_USB_PERMISSION");
                    handleActionUsbPermission(intent);
                    break;

                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Log.i(TAG, "_usbActionReceiver.onReceive() -> ACTION_USB_DEVICE_ATTACHED");
                    initUsbDevices();
                    LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                    break;

                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Log.i(TAG, "_usbActionReceiver.onReceive() -> ACTION_USB_DEVICE_DETACHED");
                    closeSerialPort();
                    LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                    break;
            }
        }

        private void handleActionUsbPermission(Intent intent) {
            boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
            if (!granted) {
                Log.w(TAG, "handleActionUsbPermission() -> Permission not granted.");
                intent.putExtra(EXTRA_USB_PERMISSION_SUCCESS, false);
                intent.putExtra(EXTRA_USB_PERMISSION_ERROR, "Permission not granted.");
                LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                return;
            }

            UsbDeviceConnection connection = _usbManager.openDevice(_usbDevice);
            if (connection == null) {
                Log.e(TAG, "handleActionUsbPermission() -> connection is null - _usbManager.openDevice() FAILED.");
                intent.putExtra(EXTRA_USB_PERMISSION_SUCCESS, false);
                intent.putExtra(EXTRA_USB_PERMISSION_ERROR, "Could not establish connection.");
                LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                return;
            }

            _serialPort = UsbSerialDevice.createUsbSerialDevice(_usbDevice, connection);
            if (_serialPort == null) {
                Log.e(TAG, "handleActionUsbPermission() -> _serialPort is null.");
                intent.putExtra(EXTRA_USB_PERMISSION_SUCCESS, false);
                intent.putExtra(EXTRA_USB_PERMISSION_ERROR, "Could not create usb serial device.");
                LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                return;
            }

            if (_serialPort.open()) {
                initSerialPort();
                Log.i(TAG, "handleActionUsbPermission() -> _serialPort open!");
                intent.putExtra(EXTRA_USB_PERMISSION_SUCCESS, true);
                LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
            } else {
                Log.e(TAG, "handleActionUsbPermission() -> _serialPort could not be opened.");
                intent.putExtra(EXTRA_USB_PERMISSION_SUCCESS, false);
                intent.putExtra(EXTRA_USB_PERMISSION_ERROR, "Serial port could not be opened.");
                LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
            }
        }

    };


    private void initSerialPort() {
        _serialPort.setBaudRate(38400);
        _serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
        _serialPort.setParity(UsbSerialInterface.PARITY_NONE);
        _serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
        _serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
        _serialPort.read(_usbReadCallback);
    }


    private void closeSerialPort() {
        try {
            _serialPort.close();
        } catch (NullPointerException e) {
            Log.w(TAG, "closeSerialPort() -> catch NullPointerException, was the port open already?");
        }
    }


    private void initUsbDevices() {
        HashMap<String, UsbDevice> usbDevices = _usbManager.getDeviceList();

        for (UsbDevice device : usbDevices.values()) {

            if (device.getVendorId() == VENDOR_ID_FTDI_USB_TO_SERIAL_CABLE) {
                _usbDevice = device;
                PendingIntent pi = PendingIntent.getBroadcast(_context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                _usbManager.requestPermission(device, pi);
                break;
            }

            Log.w(TAG, "Unknown vendorID: " + device.getVendorId());
            Log.w(TAG, "Unknown ManufacturerName: " + device.getManufacturerName());
            Log.w(TAG, "Unknown DeviceName: " + device.getDeviceName());
            Log.w(TAG, "Unknown ProductName: " + device.getProductName());
        }
    }


    UsbSerialInterface.UsbReadCallback _usbReadCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            String data;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                //TODO: do something with data
                Log.i(TAG, "_usbReadCallback.onReceivedData: " + data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * Constructor for ConnectionManager needs a Context to be able to use Android methods.
     *
     * @param context The Context object, usually an Activity.
     */
    public ConnectionManager(Context context) {
        _context = context;
        _usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        registerBroadcastReceiver(context);
    }


    private void registerBroadcastReceiver(Context context) {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        context.registerReceiver(_usbActionReceiver, filter);
    }


    /**
     * Closes all open connections and unregisters the receivers.
     *
     * @param context
     */
    public void dispose(Context context) {
        context.unregisterReceiver(_usbActionReceiver);
        closeSerialPort();
    }

}
