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

import ch.hsr.zedcontrol.roborio.ConnectionManager;
import ch.hsr.zedcontrol.roborio.RoboRIOState;

/**
 * Shows when the App is locked out from the RoboRIO due to emergency off -> App has no right to send commands.
 */
public class EmergencyFragment extends Fragment {

    private static final String TAG = EmergencyFragment.class.getSimpleName();

    private final BroadcastReceiver _connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ConnectionManager.ACTION_SERIAL_PORT_READ_STATE:
                    handleReadState(intent);
                    break;

                default:
                    Log.w(TAG, "_connectionReceiver.onReceive() -> unhandled action: " + intent.getAction());
                    break;
            }
        }

    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emergency, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(ConnectionManager.ACTION_SERIAL_PORT_READ_STATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(_connectionReceiver, filter);
    }


    private void handleReadState(final Intent intent) {
        final RoboRIOState state = (RoboRIOState) intent.getSerializableExtra(
                ConnectionManager.EXTRA_SERIAL_PORT_READ_STATE);
        Log.i(TAG, "handleReadState() -> RoboRIO state update: " + state);

        // dismiss Fragment as soon as state is not in 'emergency stop' any more
        if (!state.equals(RoboRIOState.EMERGENCY_STOP)) {
            getFragmentManager().popBackStack();
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_connectionReceiver);
    }
}
