package ch.hsr.zedcontrol;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    public static String TAG = MainFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initButtons(view);

        return view;
    }


    private void initButtons(View view) {
        initToggleButtonPowerOnOff(view);
        initButtonModeStairs(view);
        initButtonDriveFree(view);
    }


    private void initToggleButtonPowerOnOff(View view) {
        ToggleButton toggleButtonOnOff = (ToggleButton) view.findViewById(R.id.togglebutton_power);
        toggleButtonOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG, "Power ON");
                } else {
                    Log.i(TAG, "Power OFF");
                }
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
