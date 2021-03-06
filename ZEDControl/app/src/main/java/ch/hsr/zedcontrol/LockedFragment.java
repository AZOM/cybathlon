package ch.hsr.zedcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ch.hsr.zedcontrol.roborio.ConnectionManager;

/**
 * Shows when the App is locked out from the RoboRIO -> App has no right to send commands.
 */
public class LockedFragment extends Fragment {

    private static final String TAG = LockedFragment.class.getSimpleName();

    private final BroadcastReceiver _connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ConnectionManager.ACTION_SERIAL_PORT_READ_LOCK:
                    boolean hasLock = intent.getBooleanExtra(ConnectionManager.EXTRA_SERIAL_PORT_READ_LOCK, false);
                    updateUi(hasLock);
                    break;

                default:
                    Log.w(TAG, "_connectionReceiver.onReceive() -> unhandled action: " + intent.getAction());
                    break;
            }
        }

    };

    private ConnectionManager _connectionManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locked, container, false);

        initButtonReconnect(view);

        return view;
    }


    private void initButtonReconnect(View view) {
        Button button = (Button) view.findViewById(R.id.button_reconnect);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button pressed -> _connectionManager.initUsbSerialPort()");
                _connectionManager.initUsbSerialPort(getActivity());
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        // obtain instance with current state from parent activity
        _connectionManager = ((MainActivity) getActivity()).connectionManager;

        IntentFilter filter = new IntentFilter(ConnectionManager.ACTION_SERIAL_PORT_READ_LOCK);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(_connectionReceiver, filter);
    }


    private void updateUi(boolean shouldDisappear) {
        if (shouldDisappear) {
            getFragmentManager().popBackStack();
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_connectionReceiver);
    }
}
