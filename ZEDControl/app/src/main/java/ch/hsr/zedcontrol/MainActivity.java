package ch.hsr.zedcontrol;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import ch.hsr.zedcontrol.connect.ConnectionManager;

/**
 * The main full-screen activity that shows all the available controls for user interaction.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ACTION_USB_PERMISSION = ".ACTION_USB_PERMISSION";
    private static final int VENDOR_ID_FTDI_USB_TO_SERIAL_CABLE = 1027;

    private View _contentView;

    private ConnectionManager _connectionManager;
    private UsbManager _usbManager;
    private UsbDevice _usbDevice;

    private final BroadcastReceiver _usbActionReceiver = new BroadcastReceiver() {

        private UsbSerialDevice serialPort;

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_USB_PERMISSION:
                    handleActionUsbPermission(intent);
                    break;

                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Log.i(TAG, "onReceive() -> ACTION_USB_DEVICE_ATTACHED");
                    initUsbDevices();
                    break;

                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Log.i(TAG, "onReceive() -> ACTION_USB_DEVICE_DETACHED");
                    Toast.makeText(getApplicationContext(), "Device detached", Toast.LENGTH_LONG).show();
                    try {
                        serialPort.close();
                    } catch (NullPointerException e) {
                        // ignore
                    }
                    break;
            }
        }

        private void handleActionUsbPermission(Intent intent) {
            boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
            if (!granted) {
                Log.w(TAG, "Permission not granted.");
                Toast.makeText(getApplicationContext(), "No Permission (UsbManager)", Toast.LENGTH_LONG).show();
                return;
            }

            UsbDeviceConnection connection = _usbManager.openDevice(_usbDevice);
            if (connection == null) {
                Log.e(TAG, "onReceive() -> connection is null - _usbManager.openDevice() FAILED.");
                return;
            }

            serialPort = UsbSerialDevice.createUsbSerialDevice(_usbDevice, connection);
            if (serialPort == null) {
                Log.e(TAG, "Port is null.");
                Toast.makeText(getApplicationContext(), "serialPort create error", Toast.LENGTH_LONG).show();
                return;
            }

            if (serialPort.open()) {
                initSerialPort();
                Log.i(TAG, "_usbActionReceiver.onReceive() -> serialPort open!");
                Toast.makeText(getApplicationContext(), "serialPort open", Toast.LENGTH_LONG).show();

            } else {
                Log.e(TAG, "serialPort could not be opened.");
                Toast.makeText(getApplicationContext(), "serialPort open error", Toast.LENGTH_LONG).show();
            }
        }

        private void initSerialPort() {
            serialPort.setBaudRate(38400);
            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
            serialPort.read(_usbReadCallback);
        }
    };


    UsbSerialInterface.UsbReadCallback _usbReadCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] arg0) {
            String data;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                //TODO: do something with data
                Toast.makeText(getApplicationContext(), "UsbReadCallback data: " + data, Toast.LENGTH_LONG).show();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        _contentView = findViewById(R.id.layout_main);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new MainFragment())
                .commit();

        _usbManager = (UsbManager) getSystemService(USB_SERVICE);
        registerBroadcastReceiver();
    }


    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        registerReceiver(_usbActionReceiver, filter);
    }


    @Override
    protected void onResume() {
        super.onResume();

        removeStatusAndNavigationBar();
    }


    private void removeStatusAndNavigationBar() {
        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        _contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    private void initUsbDevices() {
        HashMap<String, UsbDevice> usbDevices = _usbManager.getDeviceList();

        for (UsbDevice device : usbDevices.values()) {

            if (device.getVendorId() == VENDOR_ID_FTDI_USB_TO_SERIAL_CABLE) {
                _usbDevice = device;
                PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                _usbManager.requestPermission(device, pi);
                break;
            }

            Log.w(TAG, "Unknown vendorID: " + device.getVendorId());
            Log.w(TAG, "Unknown ManufacturerName: " + device.getManufacturerName());
            Log.w(TAG, "Unknown DeviceName: " + device.getDeviceName());
            Log.w(TAG, "Unknown ProductName: " + device.getProductName());
            Toast.makeText(this, "Unknown vendorId: " + device.getVendorId(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(_usbActionReceiver);

        super.onDestroy();
    }
}
