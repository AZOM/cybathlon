package ch.hsr.zedcontrol;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Random;

/**
 * This Fragment is used to remind the user to check is current seat position in order not to lose balance.
 */
public class WarningSeatPositionFragment extends Fragment {

    public static final int DELAY_PLAY_SOUND_MS = 150;
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

    private SoundPool _soundPool;
    private int _maxVolume;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initSoundPool();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seatposition, container, false);
    }


    private void initSoundPool() {
        final AudioManager _audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        _maxVolume = _audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

        final AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        _soundPool = new SoundPool.Builder().setAudioAttributes(attributes).build();
    }


    @Override
    public void onResume() {
        super.onResume();

        playWarningSound();

        Log.i(TAG, "onResume() -> hiding warning after " + SHOW_FRAGMENT_DURATION_MS + "ms");
        _handler.postDelayed(_hideFragmentRunnable, SHOW_FRAGMENT_DURATION_MS);
    }


    private void playWarningSound() {
        final int soundId = _soundPool.load(getActivity(), getRandomWarningSound(), 1);

        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "playWarningSound() -> soundId = " + soundId);
                _soundPool.play(soundId, _maxVolume, _maxVolume, 1, 0, 1f);
            }
        }, DELAY_PLAY_SOUND_MS);
    }

    private int getRandomWarningSound() {
        int soundId = new Random().nextInt(2);

        switch (soundId) {
            case 0:
                return R.raw.industrial_alarm;
            case 1:
                return R.raw.woop_woop;
            default:
                return R.raw.industrial_alarm;
        }
    }


    @Override
    public void onDestroy() {
        _soundPool.release();
        super.onDestroy();
    }

}
