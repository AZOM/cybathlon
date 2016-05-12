package ch.hsr.zedcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import ch.hsr.zedcontrol.roborio.ConnectionManager;
import ch.hsr.zedcontrol.roborio.RoboRIOModes;

/**
 * The main full-screen activity that shows all the available controls for user interaction.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private View _contentView;

    // can be shared with Fragments - avoid a Singleton and still always have the same state.
    protected ConnectionManager connectionManager;
    private final BroadcastReceiver _connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ConnectionManager.ACTION_SERIAL_PORT_OPEN:
                    handleActionSerialPortOpen(context);
                    break;

                case ConnectionManager.ACTION_SERIAL_PORT_ERROR:
                    handleActionSerialPortError(intent);
                    break;

                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Toast.makeText(context, R.string.device_attached, Toast.LENGTH_SHORT).show();
                    break;

                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Toast.makeText(context, R.string.device_detached, Toast.LENGTH_LONG).show();
                    break;
            }
        }

        private void handleActionSerialPortOpen(Context context) {
            connectionManager.requestMode(RoboRIOModes.LOCK);
            //TODO: add isLocked based on answer in ConnectionManager._usbReadCallback for the lock (enable buttons)
            Toast.makeText(context, R.string.connected, Toast.LENGTH_LONG).show();
        }

        private void handleActionSerialPortError(Intent intent) {
            String error = intent.getStringExtra(ConnectionManager.EXTRA_SERIAL_PORT_ERROR);
            if (error != null) {
                showAlert(getString(R.string.error), error);
            } else {
                Log.w(TAG, "_connectionReceiver.onReceive() -> no error message received.");
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
    }


    @Override
    protected void onStart() {
        super.onStart();

        initConnectionReceiver();
        connectionManager = new ConnectionManager(this);
    }

    private void initConnectionReceiver() {
        IntentFilter filter = new IntentFilter(ConnectionManager.ACTION_SERIAL_PORT_OPEN);
        filter.addAction(ConnectionManager.ACTION_SERIAL_PORT_ERROR);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        LocalBroadcastManager.getInstance(this).registerReceiver(_connectionReceiver, filter);
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


    @Override
    protected void onStop() {
        super.onStop();
        //FIXME: onStop() is not called when swiping app away
        connectionManager.requestMode(RoboRIOModes.UNLOCK);
        connectionManager.dispose(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(_connectionReceiver);
    }


    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message + "\nTry to unplug and reconnect the USB cable again.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
