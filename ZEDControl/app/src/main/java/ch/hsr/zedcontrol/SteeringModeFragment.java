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
 * Container for the UI controls to change the steering mode.
 */
public class SteeringModeFragment extends Fragment {
    //TODO: Once it is decided which steering modes are necessary, implement the callbacks properly! (azo, 09.08.2016)

    private static final String TAG = SteeringModeFragment.class.getSimpleName();

    private ConnectionManager _connectionManager;

    private Button _buttonSteeringFront;
    private Button _buttonSteeringRear;
    private Button _buttonSteeringBoth;
    private Button _buttonSteeringBothMirrored;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_steering_mode, container, false);

        initButtons(view);

        return view;
    }


    private void initButtons(View view) {
        initButtonLiftFrontWheels(view);
        initButtonLiftRearWheels(view);
        initButtonLowerFrontWheels(view);
        initButtonLowerRearWheels(view);

        initButtonBack(view);
    }


    private void initButtonLiftFrontWheels(final View view) {
        _buttonSteeringFront = (Button) view.findViewById(R.id.button_steering_front);
        _buttonSteeringFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.DRIVE_THROTTLED);
                // since this is an experimental feature, we will ignore the actual callback (state change) and just
                // assume that the steering mode has been accepted and set correctly.
                selectButtonDistinct(_buttonSteeringFront);
            }
        });
    }


    private void initButtonLiftRearWheels(final View view) {
        _buttonSteeringRear = (Button) view.findViewById(R.id.button_steering_rear);
        _buttonSteeringRear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.DRIVE_THROTTLED_STEERING_REAR);
                // since this is an experimental feature, we will ignore the actual callback (state change) and just
                // assume that the steering mode has been accepted and set correctly.
                selectButtonDistinct(_buttonSteeringRear);
            }
        });
    }


    private void initButtonLowerFrontWheels(final View view) {
        _buttonSteeringBoth = (Button) view.findViewById(R.id.button_steering_both);
        _buttonSteeringBoth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.DRIVE_THROTTLED_STEERING_BOTH);
                // since this is an experimental feature, we will ignore the actual callback (state change) and just
                // assume that the steering mode has been accepted and set correctly.
                selectButtonDistinct(_buttonSteeringBoth);
            }
        });
    }


    private void initButtonLowerRearWheels(final View view) {
        _buttonSteeringBothMirrored = (Button) view.findViewById(R.id.button_steering_both_mirrored);
        _buttonSteeringBothMirrored.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()) {
                    return;
                }
                _connectionManager.sendCommand(RoboRIOCommand.DRIVE_THROTTLED_STEERING_BOTH_MIRRORED);
                // since this is an experimental feature, we will ignore the actual callback (state change) and just
                // assume that the steering mode has been accepted and set correctly.
                selectButtonDistinct(_buttonSteeringBothMirrored);
            }
        });
    }


    private void initButtonBack(View view) {
        Button button = (Button) view.findViewById(R.id.button_back_from_steering);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack(MainFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }


    private void selectButtonDistinct(Button shallBeSelectedButton) {
        if (shallBeSelectedButton.isSelected()) {
            Log.d(TAG, "selectButtonDistinct() -> IGNORE already selected button: " + shallBeSelectedButton.getText());
            return;
        }

        Log.i(TAG, "selectButtonDistinct() -> going to select button: " + shallBeSelectedButton.getText());
        _buttonSteeringFront.setSelected(_buttonSteeringFront == shallBeSelectedButton);
        _buttonSteeringRear.setSelected(_buttonSteeringRear == shallBeSelectedButton);
        _buttonSteeringBoth.setSelected(_buttonSteeringBoth == shallBeSelectedButton);
        _buttonSteeringBothMirrored.setSelected(_buttonSteeringBothMirrored == shallBeSelectedButton);
    }


    @Override
    public void onResume() {
        super.onResume();
        // obtain instance with current state from parent activity
        _connectionManager = ((MainActivity) getActivity()).connectionManager;
        initConnectionReceiver();

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
            case DRIVE_FREE:
                // steering is also front only -> let it fall through to case DRIVE_THROTTLED
            case DRIVE_THROTTLED:
                selectButtonDistinct(_buttonSteeringFront);
                break;
            case DRIVE_THROTTLED_STEERING_REAR:
                selectButtonDistinct(_buttonSteeringRear);
                break;
            case DRIVE_THROTTLED_STEERING_BOTH:
                selectButtonDistinct(_buttonSteeringBoth);
                break;
            case DRIVE_THROTTLED_STEERING_BOTH_MIRRORED:
                selectButtonDistinct(_buttonSteeringBothMirrored);
                break;
            default:
                Log.w(TAG, "updateSteeringButtons() -> ignoring state: " + currentState.name()
                        + " (not relevant for this UI)");
                break;
        }
    }


}
