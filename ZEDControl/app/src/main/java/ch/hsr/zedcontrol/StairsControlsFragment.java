package ch.hsr.zedcontrol;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ch.hsr.zedcontrol.roborio.ConnectionManager;
import ch.hsr.zedcontrol.roborio.RoboRIOModes;

/**
 * Container for the UI controls to go up and down the stairs.
 */
public class StairsControlsFragment extends Fragment {

    public static String TAG = StairsControlsFragment.class.getSimpleName();

    private ConnectionManager _connectionManager;

    private Button _buttonDisabled;
    private Button _buttonDriveModeNone;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stairs_control, container, false);

        initButtons(view);

        return view;
    }


    private void initButtons(View view) {
        initButtonLiftFrontWheels(view);
        initButtonLiftRearWheels(view);
        initButtonFallProtection(view);
        initButtonLowerFrontWheels(view);
        initButtonLowerRearWheels(view);

        initButtonBack(view);
        initButtonModeNone(view);
        _buttonDriveModeNone = (Button) view.findViewById(R.id.button_mode_none);
    }


    private void initButtonLiftFrontWheels(View view) {
        Button button = (Button) view.findViewById(R.id.button_lift_front_wheels);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: LIFT_FRONT_WHEELS");
                _connectionManager.requestMode(RoboRIOModes.LIFT_FRONT_WHEELS);
                toggleButton(v);
            }
        });
    }


    private void initButtonLiftRearWheels(View view) {
        Button button = (Button) view.findViewById(R.id.button_lift_rear_wheel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: LIFT_REAR_WHEELS");
                _connectionManager.requestMode(RoboRIOModes.LIFT_REAR_WHEELS);
                toggleButton(v);
            }
        });
    }


    private void initButtonFallProtection(View view) {
        Button button = (Button) view.findViewById(R.id.button_mode_drive_fall_protection);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: DRIVE_FALL_PROTECTION");
                _connectionManager.requestMode(RoboRIOModes.DRIVE_FALL_PROTECTION);
                toggleButton(v);
            }
        });
    }


    private void initButtonLowerFrontWheels(View view) {
        Button button = (Button) view.findViewById(R.id.button_lower_front_wheels);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: LOWER_FRONT_WHEELS");
                _connectionManager.requestMode(RoboRIOModes.LOWER_FRONT_WHEELS);
                toggleButton(v);
            }
        });
    }


    private void initButtonLowerRearWheels(View view) {
        Button button = (Button) view.findViewById(R.id.button_lower_rear_wheel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: LOWER_REAR_WHEELS");
                _connectionManager.requestMode(RoboRIOModes.LOWER_REAR_WHEELS);
                toggleButton(v);
            }
        });
    }


    private void initButtonBack(View view) {
        Button button = (Button) view.findViewById(R.id.button_back);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack(MainFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
                toggleButton(v);
            }
        });
    }


    private void toggleButton(View v) {
        if (_buttonDisabled != null) {
            _buttonDisabled.setEnabled(true);
        }
        v.setEnabled(false);
        _buttonDisabled = (Button) v;
    }


    @Override
    public void onResume() {
        super.onResume();
        // obtain instance with current state from parent activity
        _connectionManager = ((MainActivity) getActivity()).connectionManager;

        _buttonDriveModeNone.performClick();
    }

}
