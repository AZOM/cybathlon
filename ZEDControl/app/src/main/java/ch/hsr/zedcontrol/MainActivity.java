package ch.hsr.zedcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ch.hsr.zedcontrol.roborio.ConnectionManager;
import ch.hsr.zedcontrol.roborio.RoboRIOModes;

/**
 * The main full-screen activity that shows all the available controls for user interaction.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int VENDOR_ID_FTDI_USB_TO_SERIAL_CABLE = 1027;

    // can be shared with Fragments - avoid a Singleton and still always have the same state.
    protected ConnectionManager connectionManager;
    protected boolean hasLock = false; //production = false;

    private View _contentView;

    private final BroadcastReceiver _connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ConnectionManager.ACTION_SERIAL_PORT_OPEN:
                    connectionManager.requestMode(RoboRIOModes.LOCK);
                    break;

                case ConnectionManager.ACTION_SERIAL_PORT_ERROR:
                    handleActionSerialPortError(intent);
                    break;

                case ConnectionManager.ACTION_SERIAL_PORT_READ_LOCK:
                    hasLock = intent.getBooleanExtra(ConnectionManager.EXTRA_SERIAL_PORT_READ_LOCK, false);
                    invalidateUi();
                    break;

                case ConnectionManager.ACTION_SERIAL_PORT_READ_MODE:
                    handleActionSerialPortReadMode(intent);
                    break;

                case ConnectionManager.ACTION_SERIAL_PORT_READ_BATTERY:
                    updateUiVoltage(intent);
                    break;

                default:
                    Log.w(TAG, "_connectionReceiver.onReceive() -> unhandled action: " + intent.getAction());
                    break;
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
        connectionManager = new ConnectionManager(this, VENDOR_ID_FTDI_USB_TO_SERIAL_CABLE);
        connectionManager.scanUsbDevicesForVendorId(this, VENDOR_ID_FTDI_USB_TO_SERIAL_CABLE);
    }


    private void initConnectionReceiver() {
        IntentFilter filter = new IntentFilter(ConnectionManager.ACTION_SERIAL_PORT_OPEN);
        filter.addAction(ConnectionManager.ACTION_SERIAL_PORT_ERROR);
        filter.addAction(ConnectionManager.ACTION_SERIAL_PORT_READ_LOCK);
        filter.addAction(ConnectionManager.ACTION_SERIAL_PORT_READ_MODE);
        filter.addAction(ConnectionManager.ACTION_SERIAL_PORT_READ_BATTERY);

        LocalBroadcastManager.getInstance(this).registerReceiver(_connectionReceiver, filter);
    }


    @Override
    protected void onResume() {
        super.onResume();

        removeStatusAndNavigationBar();
        invalidateUi();
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


    private void invalidateUi() {
        Fragment activeFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (activeFragment instanceof ControlsFragment) {
            ((ControlsFragment) activeFragment).enableDisableView(hasLock);
        }

        if (hasLock) {
            Toast.makeText(this, R.string.connected, Toast.LENGTH_LONG).show();
        } else {
            showLockedFragment();
        }
    }


    private void showLockedFragment() {
        Fragment activeFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (activeFragment instanceof LockedFragment) {
            // nothing to do
        } else {
            //TODO: animate?
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LockedFragment())
                    .addToBackStack(TAG)
                    .commit();
        }
    }


    private void handleActionSerialPortError(Intent intent) {
        String errorMessage = intent.getStringExtra(ConnectionManager.EXTRA_SERIAL_PORT_ERROR);
        showErrorAlert(getString(R.string.error), errorMessage);

        if (errorMessage == null) {
            Log.wtf(TAG, "handleActionSerialPortError() -> Missing errorMessage.");
        }
    }


    private void showErrorAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(getString(R.string.reinitialize), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        connectionManager.requestMode(RoboRIOModes.START_UP);
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void handleActionSerialPortReadMode(Intent intent) {
        RoboRIOModes mode = (RoboRIOModes) intent.getSerializableExtra(ConnectionManager.EXTRA_SERIAL_PORT_READ_MODE);
        Toast.makeText(this, "ACK: " + mode.name(), Toast.LENGTH_SHORT).show();
    }


    private void updateUiVoltage(Intent intent) {
        String voltageString = intent.getStringExtra(ConnectionManager.EXTRA_SERIAL_PORT_READ_BATTERY);
        TextView tv = (TextView) findViewById(R.id.battery_voltage);
        if (tv != null) {
            tv.setText(voltageString);
        }
    }

}
