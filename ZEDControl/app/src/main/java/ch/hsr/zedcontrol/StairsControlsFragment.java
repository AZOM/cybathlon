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
import android.widget.Toast;

/**
 * Container for the UI controls to go up and down the stairs.
 */
public class StairsControlsFragment extends Fragment {

    public static String TAG = StairsControlsFragment.class.getSimpleName();

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

        initButtonDriveFree(view);
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
                Log.i(TAG, "pop back to MainFragment and activate free driving");
                Toast.makeText(getActivity(), R.string.toast_activate_free_driving, Toast.LENGTH_LONG).show();

                getFragmentManager().popBackStack(MainFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }
}
