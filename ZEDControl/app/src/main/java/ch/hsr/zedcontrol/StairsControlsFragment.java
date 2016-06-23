package ch.hsr.zedcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ch.hsr.zedcontrol.roborio.ConnectionManager;
import ch.hsr.zedcontrol.roborio.RoboRIOCommand;
import ch.hsr.zedcontrol.roborio.RoboRIOState;

/**
 * Container for the UI controls to go up and down the stairs.
 */
public class StairsControlsFragment extends Fragment {

    private static final String TAG = StairsControlsFragment.class.getSimpleName();

    private ConnectionManager _connectionManager;

    private Button _buttonLiftFrontWheels;
    private Button _buttonLiftRearWheels;
    private Button _buttonDriveWithFallProtection;
    private Button _buttonLowerFrontWheels;
    private Button _buttonLowerRearWheels;
    private Button _buttonNoMode;

    private final BroadcastReceiver _connectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ConnectionManager.ACTION_SERIAL_PORT_READ_STATE:
                    RoboRIOState state = (RoboRIOState) intent.getSerializableExtra(
                            ConnectionManager.EXTRA_SERIAL_PORT_READ_STATE);
                    handleStateChanged(state);
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
        initButtonNoMode(view);
    }


    private void initButtonLiftFrontWheels(final View view) {
        _buttonLiftFrontWheels = (Button) view.findViewById(R.id.button_lift_front_wheels);
        _buttonLiftFrontWheels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.LIFT_FRONT_WHEELS);
            }
        });
    }


    private void initButtonLiftRearWheels(final View view) {
        _buttonLiftRearWheels = (Button) view.findViewById(R.id.button_lift_rear_wheel);
        _buttonLiftRearWheels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.LIFT_REAR_WHEELS);
            }
        });
    }


    private void initButtonFallProtection(final View view) {
        _buttonDriveWithFallProtection = (Button) view.findViewById(R.id.button_mode_drive_fall_protection);
        _buttonDriveWithFallProtection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.DRIVE_FALL_PROTECTION);
            }
        });
    }


    private void initButtonLowerFrontWheels(final View view) {
        _buttonLowerFrontWheels = (Button) view.findViewById(R.id.button_lower_front_wheels);
        _buttonLowerFrontWheels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.LOWER_FRONT_WHEELS);
            }
        });
    }


    private void initButtonLowerRearWheels(final View view) {
        _buttonLowerRearWheels = (Button) view.findViewById(R.id.button_lower_rear_wheel);
        _buttonLowerRearWheels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.LOWER_REAR_WHEELS);
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


    private void initButtonNoMode(View view) {
        _buttonNoMode = (Button) view.findViewById(R.id.button_no_mode);
        _buttonNoMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.NO_MODE);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        // obtain instance with current state from parent activity
        _connectionManager = ((MainActivity) getActivity()).connectionManager;
        initConnectionReceiver();

        _buttonNoMode.performClick();
    }


    private void initConnectionReceiver() {
        IntentFilter filter = new IntentFilter(ConnectionManager.ACTION_SERIAL_PORT_READ_STATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(_connectionReceiver, filter);
    }


    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_connectionReceiver);
    }


    private void handleStateChanged(RoboRIOState newState) {
        switch (newState) {
            case LIFT_FRONT_WHEELS:
                selectButtonDistinct(_buttonLiftFrontWheels);
                break;
            case LIFT_REAR_WHEELS:
                selectButtonDistinct(_buttonLiftRearWheels);
                break;
            case DRIVE_FALL_PROTECTION:
                selectButtonDistinct(_buttonDriveWithFallProtection);
                break;
            case LOWER_FRONT_WHEELS:
                selectButtonDistinct(_buttonLowerFrontWheels);
                break;
            case LOWER_REAR_WHEELS:
                selectButtonDistinct(_buttonLowerRearWheels);
                break;
            case NO_MODE:
                selectButtonDistinct(_buttonNoMode);
                break;
            default:
                Log.w(TAG, "handleStateChanged() -> ignored state: " + newState.name() + " (not relevant for this UI)");
        }
    }


    private void selectButtonDistinct(Button shallBeSelectedButton) {
        if (shallBeSelectedButton.isSelected()) {
            Log.d(TAG, "selectButtonDistinct() -> IGNORE already selected button: " + shallBeSelectedButton.getText());
        }

        Log.i(TAG, "selectButtonDistinct() -> going to select button: " + shallBeSelectedButton.getText());
        _buttonNoMode.setSelected(_buttonNoMode == shallBeSelectedButton);
    }

}
