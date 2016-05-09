package ch.hsr.zedcontrol;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

/**
 * Container for the main UI controls.
 */
public class MainFragment extends Fragment {

    private static String TAG = MainFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initButtons(view);

        return view;
    }

    private void initButtons(View view) {
        initToggleButtonPowerOnOff(view);
        initButtonLiftFrontWheels(view);
        initButtonLiftRearWheels(view);
        initButtonFallProtection(view);
        initButtonLowerFrontWheels(view);
        initButtonLowerRearWheels(view);
        initButtonDriveFree(view);
    }

    private void initToggleButtonPowerOnOff(View view) {
        ToggleButton toggleButtonOnOff = (ToggleButton) view.findViewById(R.id.togglebutton_power);
        toggleButtonOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG, "power should be ON");
                } else {
                    Log.i(TAG, "power should be OFF");
                }
            }
        });
    }

    private void initButtonLiftFrontWheels(View view) {
        Button button = (Button) view.findViewById(R.id.button_lift_front_wheels);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Should LIFT front wheels");
            }
        });
    }

    private void initButtonLiftRearWheels(View view) {
        Button button = (Button) view.findViewById(R.id.button_lift_rear_wheel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Should LIFT rear wheels");
            }
        });
    }

    private void initButtonFallProtection(View view) {
        Button button = (Button) view.findViewById(R.id.button_fall_protection);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Should enable driving with fall protection");
            }
        });
    }

    private void initButtonLowerFrontWheels(View view) {
        Button button = (Button) view.findViewById(R.id.button_lower_front_wheels);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Should LOWER front wheels");
            }
        });
    }

    private void initButtonLowerRearWheels(View view) {
        Button button = (Button) view.findViewById(R.id.button_lower_rear_wheel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Should LOWER rear wheels");
            }
        });
    }

    private void initButtonDriveFree(View view) {
        Button button = (Button) view.findViewById(R.id.button_driving_free);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Should disable driving with fall protection - FREE MODE");
            }
        });
    }
}
