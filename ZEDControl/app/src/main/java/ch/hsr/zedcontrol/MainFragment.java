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
import android.widget.Toast;

import ch.hsr.zedcontrol.roborio.ConnectionManager;
import ch.hsr.zedcontrol.roborio.RoboRIOCommand;
import ch.hsr.zedcontrol.roborio.RoboRIOState;

/**
 * Container for the main UI controls.
 */
public class MainFragment extends Fragment {

    protected static final String TAG = MainFragment.class.getSimpleName();

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

    private ConnectionManager _connectionManager;

    private Button _buttonPowerOff;
    private Button _buttonStartUp;
    private Button _buttonDriveThrottled;
    private Button _buttonDriveFast;
    private Button _buttonNoMode;


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
        initButtonDriveSlow(view);
        initButtonDriveFast(view);
        initButtonStairs(view);
        initButtonNoMode(view);
    }


    private void initButtonPowerOff(View view) {
        _buttonPowerOff = (Button) view.findViewById(R.id.button_power_off);
        _buttonPowerOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.POWER_OFF);
            }
        });
    }


    private void initButtonStartUp(View view) {
        _buttonStartUp = (Button) view.findViewById(R.id.button_start_up);
        _buttonStartUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.START_UP);
            }
        });
    }


    private void initButtonDriveSlow(View view) {
        _buttonDriveThrottled = (Button) view.findViewById(R.id.button_drive_slow);
        _buttonDriveThrottled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.DRIVE_THROTTLED);
            }
        });
    }


    private void initButtonDriveFast(View view) {
        _buttonDriveFast = (Button) view.findViewById(R.id.button_drive_fast);
        _buttonDriveFast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.DRIVE_FREE);
            }
        });
    }


    private void initButtonStairs(View view) {
        Button button = (Button) view.findViewById(R.id.button_driving_stairs);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                                R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.fragment_container, new StairsControlsFragment())
                        .addToBackStack(TAG)
                        .commit();
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
            case POWER_OFF:
                selectButtonDistinct(_buttonPowerOff);
                break;
            case START_UP:
                selectButtonDistinct(_buttonStartUp);
                break;
            case DRIVE_FREE:
                selectButtonDistinct(_buttonDriveFast);
                break;
            case DRIVE_THROTTLED:
                selectButtonDistinct(_buttonDriveThrottled);
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
            Log.d(TAG, "selectButtonDistinct() -> IGNORE, already selected: " + shallBeSelectedButton.getText());
            return;
        }

        Log.i(TAG, "selectButtonDistinct() -> going to select button: " + shallBeSelectedButton.getText());
        _buttonPowerOff.setSelected(_buttonPowerOff == shallBeSelectedButton);
        _buttonStartUp.setSelected(_buttonStartUp == shallBeSelectedButton);
        _buttonDriveThrottled.setSelected(_buttonDriveThrottled == shallBeSelectedButton);
        _buttonDriveFast.setSelected(_buttonDriveFast == shallBeSelectedButton);
        _buttonNoMode.setSelected(_buttonNoMode == shallBeSelectedButton);
    }
}
