package ch.hsr.zedcontrol;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import ch.hsr.zedcontrol.roborio.ConnectionManager;
import ch.hsr.zedcontrol.roborio.RoboRIOCommand;

/**
 * Container for the UI controls to go up and down the stairs.
 */
public class StairsControlsFragment extends Fragment {

    private static final String TAG = StairsControlsFragment.class.getSimpleName();

    private ConnectionManager _connectionManager;

    private ToggleButton _buttonDriveModeNone;
    private Button _activeStairsModeButton;


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
    }


    private void initButtonLiftFrontWheels(final View view) {
        Button button = (Button) view.findViewById(R.id.button_lift_front_wheels);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: LIFT_FRONT_WHEELS");
                _connectionManager.requestMode(RoboRIOCommand.LIFT_FRONT_WHEELS);

                updateDriveModeButtons((Button) v);

                // enable next state
                view.findViewById(R.id.button_lift_rear_wheel).setEnabled(true);
            }
        });
    }


    private void initButtonLiftRearWheels(final View view) {
        Button button = (Button) view.findViewById(R.id.button_lift_rear_wheel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: LIFT_REAR_WHEELS");
                _connectionManager.requestMode(RoboRIOCommand.LIFT_REAR_WHEELS);

                updateDriveModeButtons((Button) v);

                // enable next state
                view.findViewById(R.id.button_mode_drive_fall_protection).setEnabled(true);
            }
        });
    }


    private void initButtonFallProtection(final View view) {
        Button button = (Button) view.findViewById(R.id.button_mode_drive_fall_protection);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: DRIVE_FALL_PROTECTION");
                _connectionManager.requestMode(RoboRIOCommand.DRIVE_FALL_PROTECTION);

                updateDriveModeButtons((Button) v);

                // enable next state
                view.findViewById(R.id.button_lower_front_wheels).setEnabled(true);
            }
        });
    }


    private void initButtonLowerFrontWheels(final View view) {
        Button button = (Button) view.findViewById(R.id.button_lower_front_wheels);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: LOWER_FRONT_WHEELS");
                _connectionManager.requestMode(RoboRIOCommand.LOWER_FRONT_WHEELS);

                updateDriveModeButtons((Button) v);

                // enable next state
                view.findViewById(R.id.button_lower_rear_wheel).setEnabled(true);
            }
        });
    }


    private void initButtonLowerRearWheels(final View view) {
        Button button = (Button) view.findViewById(R.id.button_lower_rear_wheel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: LOWER_REAR_WHEELS");
                _connectionManager.requestMode(RoboRIOCommand.LOWER_REAR_WHEELS);

                updateDriveModeButtons((Button) v);

                // enable first state again
                view.findViewById(R.id.button_lift_front_wheels).setEnabled(true);
            }
        });
    }


    private void updateDriveModeButtons(Button clickedButton) {
        if (_buttonDriveModeNone != null) {
            _buttonDriveModeNone.setChecked(false);
        }

        clickedButton.setEnabled(false);
        clickedButton.getBackground().setColorFilter(
                getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);

        if (_activeStairsModeButton != null) {
            // change color last state
            _activeStairsModeButton.getBackground().setColorFilter(
                    getResources().getColor(android.R.color.background_light), PorterDuff.Mode.MULTIPLY);
        }

        _activeStairsModeButton = clickedButton;
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
        ToggleButton button = (ToggleButton) view.findViewById(R.id.button_mode_none);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (_activeStairsModeButton != null) {
                        _activeStairsModeButton.getBackground().setColorFilter(
                                getResources().getColor(android.R.color.background_light), PorterDuff.Mode.MULTIPLY);
                    }

                    Log.i(TAG, "Requesting mode: NO_MODE");
                    _connectionManager.requestMode(RoboRIOCommand.NO_MODE);

                } else {
                    if (_activeStairsModeButton != null) {
                        _activeStairsModeButton.getBackground().setColorFilter(
                                getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                    }
                }
            }
        });

        _buttonDriveModeNone = button;
    }


    @Override
    public void onResume() {
        super.onResume();
        // obtain instance with current state from parent activity
        _connectionManager = ((MainActivity) getActivity()).connectionManager;

        _buttonDriveModeNone.setChecked(true);
    }

}
