package ch.hsr.zedcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
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

    private static final double THRESHOLD_VOLTAGE = 24.4d;

    // can be shared with Fragments - avoid a Singleton and still always have the same state.
    protected ConnectionManager connectionManager;

    private View _contentView;

    private boolean _hasLock = false;
    private boolean _isShowingAlertDialog = false;

    private final BroadcastReceiver _connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ConnectionManager.ACTION_SERIAL_PORT_ERROR:
                    handleActionSerialPortError(intent);
                    break;

                case ConnectionManager.ACTION_SERIAL_PORT_READ_LOCK:
                    _hasLock = intent.getBooleanExtra(ConnectionManager.EXTRA_SERIAL_PORT_READ_LOCK, false);
                    updateUiLockChanged();
                    break;

                case ConnectionManager.ACTION_SERIAL_PORT_READ_MODE:
                    handleActionSerialPortReadMode(intent);
                    break;

                case ConnectionManager.ACTION_SERIAL_PORT_READ_BATTERY:
                    updateUiVoltage(intent);
                    break;

                case ConnectionManager.ACTION_SERIAL_PORT_READ_STATE:
                    handleActionSerialPortReadState(intent);
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

        if (savedInstanceState == null) {
            // everything else that doesn't update UI
            initConnectionReceiver();
            connectionManager = new ConnectionManager(this);
        }
    }


    private void initConnectionReceiver() {
        IntentFilter filter = new IntentFilter(ConnectionManager.ACTION_SERIAL_PORT_ERROR);
        filter.addAction(ConnectionManager.ACTION_SERIAL_PORT_READ_LOCK);
        filter.addAction(ConnectionManager.ACTION_SERIAL_PORT_READ_MODE);
        filter.addAction(ConnectionManager.ACTION_SERIAL_PORT_READ_BATTERY);
        filter.addAction(ConnectionManager.ACTION_SERIAL_PORT_READ_STATE);

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
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(_connectionReceiver);
        //FIXME: onDestroy() is not called when swiping app away
        connectionManager.requestMode(RoboRIOModes.UNLOCK);
        connectionManager.dispose(this);
    }


    private void updateUiLockChanged() {
        if (_hasLock) {
            Toast.makeText(this, R.string.connected, Toast.LENGTH_LONG).show();
        } else {
            showLockedFragment();
        }
    }


    private void showLockedFragment() {
        final Fragment activeFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (!(activeFragment instanceof LockedFragment)) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new LockedFragment())
                    .addToBackStack(TAG)
                    .commit();
        }
    }


    private void handleActionSerialPortError(Intent intent) {
        if (_isShowingAlertDialog) {
            // Avoid multiple AlertDialogs
            return;
        }

        String errorMessage = intent.getStringExtra(ConnectionManager.EXTRA_SERIAL_PORT_ERROR);
        showAlertDialog(getString(R.string.error), errorMessage);
        _isShowingAlertDialog = true;

        if (errorMessage == null) {
            Log.wtf(TAG, "handleActionSerialPortError() -> Missing errorMessage.");
        }
    }


    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        _isShowingAlertDialog = false;
                    }
                })
                .setPositiveButton(getString(R.string.reinitialize), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        connectionManager.requestMode(RoboRIOModes.START_UP);
                        dialog.cancel();
                        _isShowingAlertDialog = false;
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void handleActionSerialPortReadMode(Intent intent) {
        RoboRIOModes mode = (RoboRIOModes) intent.getSerializableExtra(ConnectionManager.EXTRA_SERIAL_PORT_READ_MODE);
        Log.i(TAG, "handleActionSerialPortReadMode() -> Got ACK for requested mode: " + mode);
        Toast.makeText(this, "ACK: " + mode.name(), Toast.LENGTH_SHORT).show();
    }


    private void updateUiVoltage(Intent intent) {
        double voltage = intent.getDoubleExtra(ConnectionManager.EXTRA_SERIAL_PORT_READ_BATTERY, -1d);
        TextView tv = (TextView) findViewById(R.id.battery_voltage);
        if (tv != null) {
            tv.setText(getString(R.string.string_voltage, voltage));

            if (voltage <= THRESHOLD_VOLTAGE) {
                tv.setTextColor(Color.RED);
            } else {
                tv.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            }
        }
    }


    private void handleActionSerialPortReadState(Intent intent) {
        RoboRIOModes mode = (RoboRIOModes) intent.getSerializableExtra(ConnectionManager.EXTRA_SERIAL_PORT_READ_STATE);
        Log.i(TAG, "handleActionSerialPortReadState -> keep-alive signal for state: " + mode);

        //TODO: check if it is EmergencyOff
    }


    @Override
    public void onBackPressed() {
        final Fragment activeFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (activeFragment instanceof LockedFragment) {
            // ignore back press -> avoid a popBackStack here, since the User cannot use any controls in locked state
            Log.i(TAG, "onBackPressed() -> suppressed because LockedFragment is active.");
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // ignore the volume keys as long as the MainActivity is active
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
