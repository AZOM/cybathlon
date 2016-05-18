package ch.hsr.zedcontrol;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment that contains elements which are related to to control the wheel-chair and need to be able to enable/disable
 * those controls.
 */
public abstract class ControlsFragment extends Fragment {

    /**
     * Get the parent view where all the controls are that can be toggled upon certain events.
     *
     * @return The parent-view for the controls of this Fragment.
     */
    abstract View getControlsView();

    protected void enableDisableView(boolean enabled) {
        enableDisableView(getControlsView(), enabled);
    }


    /**
     * Enables or disables all children of a given view.
     * Taken from: http://stackoverflow.com/a/5257691/3540885
     *
     * @param view    The view where all the children shall be enabled/disabled
     * @param enabled Defines whether enabled or disabled
     */
    protected void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int idx = 0; idx < group.getChildCount(); idx++) {
                enableDisableView(group.getChildAt(idx), enabled);
            }
        }
    }
}
