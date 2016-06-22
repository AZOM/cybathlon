package ch.hsr.zedcontrol;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import ch.hsr.zedcontrol.roborio.ConnectionManager;
import ch.hsr.zedcontrol.roborio.RoboRIOCommand;

/**
 * Container for the main UI controls.
 */
public class MainFragment extends Fragment {

    public static String TAG = MainFragment.class.getSimpleName();

    private ConnectionManager _connectionManager;

    private ToggleButton _buttonDefaultDriveMode;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initButtons(view);

        _buttonDefaultDriveMode = (ToggleButton) view.findViewById(R.id.button_mode_none);

        return view;
    }


    private void initButtons(View view) {
        initButtonPowerOff(view);
        initButtonStartUp(view);
        initButtonModeStairs(view);

        initButtonsDriveModes(view);
    }


    private void initButtonPowerOff(View view) {
        Button button = (Button) view.findViewById(R.id.button_power_off);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: POWER_OFF");
                _connectionManager.requestMode(RoboRIOCommand.POWER_OFF);
            }
        });
    }


    private void initButtonStartUp(View view) {
        Button button = (Button) view.findViewById(R.id.button_start_up);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: START_UP");
                _connectionManager.requestMode(RoboRIOCommand.START_UP);
            }
        });
    }


    private void initButtonModeStairs(View view) {
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


    private void initButtonsDriveModes(View view) {
        initToggleButtonModeFreeDriving(view);
        initToggleButtonModeNone(view);
        initRadioGroup(view);
    }


    private void initToggleButtonModeFreeDriving(View view) {
        final ToggleButton button = (ToggleButton) view.findViewById(R.id.button_mode_free_driving);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: DRIVE_FREE");
                _connectionManager.requestMode(RoboRIOCommand.DRIVE_FREE);

                markAsCheckedWithinRadioGroup(v);
            }
        });
    }


    private void markAsCheckedWithinRadioGroup(View v) {
        // let the ToggleButton behave like a RadioButton -> only one can be active at a time
        ((RadioGroup) v.getParent()).check(0);
        ((RadioGroup) v.getParent()).check(v.getId());
    }


    private void initToggleButtonModeNone(View view) {
        final ToggleButton button = (ToggleButton) view.findViewById(R.id.button_mode_none);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Requesting mode: NO_MODE");
                _connectionManager.requestMode(RoboRIOCommand.NO_MODE);

                markAsCheckedWithinRadioGroup(v);
            }
        });
    }


    private void initRadioGroup(View view) {
        ((RadioGroup) view.findViewById(R.id.group_drive_mode)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int index = 0; index < group.getChildCount(); index++) {
                    final ToggleButton b = (ToggleButton) group.getChildAt(index);
                    b.setChecked(b.getId() == checkedId);
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        // obtain instance with current state from parent activity
        _connectionManager = ((MainActivity) getActivity()).connectionManager;

        _buttonDefaultDriveMode.performClick();
    }


}
