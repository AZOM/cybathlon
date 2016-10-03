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

    private Button _buttonDriveWithFixedSteering;
    private Button _buttonLiftFrontWheels;
    private Button _buttonLiftRearWheels;
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
        initButtonDriveWithFixedSteering(view);

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

                showWarningSeatPosition();
            }
        });
    }


    private void showWarningSeatPosition() {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_overlay, new WarningSeatPositionFragment())
                .addToBackStack(TAG)
                .commit();
    }


    private void initButtonDriveWithFixedSteering(final View view) {
        _buttonDriveWithFixedSteering = (Button) view.findViewById(R.id.button_drive_fixed_steering);
        _buttonDriveWithFixedSteering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.DRIVE_FIXED_STEERING);
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

        // ensure that no driving mode is active when (re-)entering this fragment (security reason)
        _connectionManager.sendCommand(RoboRIOCommand.NO_MODE);
        // used to have the button "no mode" immediately selected when returning from EmergencyFragment
        handleStateChanged(_connectionManager.getCurrentState());
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


    private void handleStateChanged(final RoboRIOState currentState) {
        switch (currentState) {
            case DRIVE_FIXED_STEERING:
                selectButtonDistinct(_buttonDriveWithFixedSteering);
                break;
            case LIFT_FRONT_WHEELS:
                selectButtonDistinct(_buttonLiftFrontWheels);
                break;
            case LIFT_REAR_WHEELS:
                selectButtonDistinct(_buttonLiftRearWheels);
                break;
            case NO_MODE:
                selectButtonDistinct(_buttonNoMode);
                break;
            default:
                Log.w(TAG, "handleStateChanged() -> ignoring state: " + currentState.name()
                        + " (not relevant for this UI)");
        }
    }


    private void selectButtonDistinct(Button shallBeSelectedButton) {
        if (shallBeSelectedButton.isSelected()) {
            Log.d(TAG, "selectButtonDistinct() -> IGNORE already selected button: " + shallBeSelectedButton.getText());
            return;
        }

        Log.i(TAG, "selectButtonDistinct() -> going to select button: " + shallBeSelectedButton.getText());
        _buttonDriveWithFixedSteering.setSelected(_buttonDriveWithFixedSteering == shallBeSelectedButton);
        _buttonLiftFrontWheels.setSelected(_buttonLiftFrontWheels == shallBeSelectedButton);
        _buttonLiftRearWheels.setSelected(_buttonLiftRearWheels == shallBeSelectedButton);
        _buttonNoMode.setSelected(_buttonNoMode == shallBeSelectedButton);
    }

}
