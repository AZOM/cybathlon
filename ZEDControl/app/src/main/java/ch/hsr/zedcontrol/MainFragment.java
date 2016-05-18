package ch.hsr.zedcontrol;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ch.hsr.zedcontrol.roborio.ConnectionManager;
import ch.hsr.zedcontrol.roborio.RoboRIOModes;

/**
 * Container for the main UI controls.
 */
public class MainFragment extends ControlsFragment {

    public static String TAG = MainFragment.class.getSimpleName();

    private ConnectionManager _connectionManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initButtons(view);

        return view;
    }


    private void initButtons(View view) {
        initButtonPowerOff(view);
        initButtonStartUp(view);
        initButtonModeFreeDriving(view);
        initButtonModeNone(view);
        initButtonModeStairs(view);
    }


    private void initButtonPowerOff(View view) {
        Button button = (Button) view.findViewById(R.id.button_power_off);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: POWER_OFF");
                _connectionManager.requestMode(RoboRIOModes.POWER_OFF);
            }
        });
    }


    private void initButtonStartUp(View view) {
        Button button = (Button) view.findViewById(R.id.button_start_up);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: START_UP");
                _connectionManager.requestMode(RoboRIOModes.START_UP);
            }
        });
    }


    private void initButtonModeFreeDriving(View view) {
        Button button = (Button) view.findViewById(R.id.button_mode_free_driving);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: DRIVE_FREE");
                _connectionManager.requestMode(RoboRIOModes.DRIVE_FREE);
            }
        });
    }


    private void initButtonModeNone(View view) {
        Button button = (Button) view.findViewById(R.id.button_mode_none);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: NO_MODE");
                _connectionManager.requestMode(RoboRIOModes.NO_MODE);
            }
        });
    }


    private void initButtonModeStairs(View view) {
        Button button = (Button) view.findViewById(R.id.button_driving_stairs);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new StairsControlsFragment())
                        .addToBackStack(TAG)
                        .commit();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        // obtain instance with current state from parent activity
        _connectionManager = ((MainActivity) getActivity()).connectionManager;

        boolean shouldEnable = ((MainActivity) getActivity()).hasLock;
        enableDisableView(shouldEnable);
    }


    @Override
    View getControlsView() {
        return getView().findViewById(R.id.layout_buttons);
    }
}
