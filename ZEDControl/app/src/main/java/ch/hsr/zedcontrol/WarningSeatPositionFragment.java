package ch.hsr.zedcontrol;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This Fragment is used to remind the user to check is current seat position in order not to lose balance.
 */
public class WarningSeatPositionFragment extends Fragment {

    private static final String TAG = WarningSeatPositionFragment.class.getSimpleName();
    private static final int SHOW_FRAGMENT_DURATION_MS = 3000;

    private final Handler _handler = new Handler();
    private final Runnable _hideFragmentRunnable = new Runnable() {
        @Override
        public void run() {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                Log.i(TAG, "_hideFragmentRunnable.run() -> getFragmentManager().popBackStack()");
                getFragmentManager().popBackStack();
            } else {
                Log.w(TAG, "_hideFragmentRunnable.run() -> avoiding Nullpointer Exception when popBackStack()");
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seatposition, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "onResume() -> hiding warning after " + SHOW_FRAGMENT_DURATION_MS + "ms");
        _handler.postDelayed(_hideFragmentRunnable, SHOW_FRAGMENT_DURATION_MS);
    }

}
