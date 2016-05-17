package ch.hsr.zedcontrol.roborio;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Parses the data from UsbSerialInterface.UsbReadCallback to something meaningful.
 */
public class RoboRIOStateParser {

    private static final String TAG = RoboRIOStateParser.class.getSimpleName();

    public static ArrayList<String> parse(@NonNull String data) throws RoboRIOLockException, RoboRIOStateException, RoboRIOModeException {
        ArrayList<String> result = new ArrayList<>();

        // Split string into separate messages, since data can be like: Lock:false:;State::0:false:;
        final String[] lines = data.split(";");

        for (String line : lines) {
            final String parsedLine = parseLine(line);

            if (parsedLine != null) {
                result.add(parsedLine);
            }
        }

        return result;
    }

    @Nullable
    private static String parseLine(@NonNull String line) throws RoboRIOStateException, RoboRIOLockException, RoboRIOModeException {
        final String[] words = line.split(":");

        switch (words[0]) {
            case "Battery":
                return new BatteryData(words).voltage + " V";

            case "State":
                return parseStateData(words);

            case "Lock":
            case "Unlock":
                return parseLockData(words);

            case "Mode":
                return parseModeData(words);

            default:
                Log.wtf(TAG, "parseLine() -> could not parse line for: " + words[0]);
                return null;
        }
    }


    // Used because we want a different Exception here - else it is the same as parseModeData
    private static String parseStateData(String[] words) throws RoboRIOStateException {
        final ModeData stateData = new ModeData(words);

        if (stateData.hasError) {
            throw new RoboRIOStateException(stateData.errorMessage);
        }

        return stateData.getModeDescription();
    }


    private static String parseLockData(String[] words) throws RoboRIOLockException {
        final LockData lockData = new LockData(words);

        if (lockData.hasError) {
            throw new RoboRIOLockException(lockData.errorMessage);
        }

        return lockData.getLockDescription();
    }


    private static String parseModeData(String[] words) throws RoboRIOModeException {
        final ModeData modeData = new ModeData(words);

        if (modeData.hasError) {
            throw new RoboRIOModeException(modeData.errorMessage);
        }

        return modeData.getModeDescription();
    }


    private static class BatteryData {
        protected final String keyWord;
        protected final double voltage;

        public BatteryData(String[] words) {
            if (words.length < 2) {
                throw new IllegalArgumentException("BatteryData: expected length of 2 but was: " + words.length);
            }

            keyWord = words[0];
            voltage = Double.parseDouble(words[1]);
        }
    }


    private static class LockData {
        protected final String keyWord;
        protected final boolean hasError;
        protected String errorMessage;

        public LockData(String[] words) {
            if (words.length < 2) {
                throw new IllegalArgumentException("LockData: expected at least length of 2 but was: " + words.length);
            }

            keyWord = words[0];
            hasError = Boolean.parseBoolean(words[1]);
            if (hasError) {
                if (words.length < 3) {
                    throw new IllegalArgumentException("LockData: expected length of 3 but was: " + words.length);
                }
                errorMessage = words[2];
            }
        }

        public String getLockDescription() {
            return keyWord + ";";
        }
    }


    private static class ModeData {
        protected final String keyWord;
        protected final String modeName;
        protected final int subModeNr;
        protected final boolean hasError;
        protected String errorMessage;

        public ModeData(String[] words) {
            if (words.length < 4) {
                throw new IllegalArgumentException("ModeData: expected at least length of 4 but was: " + words.length);
            }

            keyWord = words[0];
            modeName = words[1];
            subModeNr = Integer.parseInt(words[2]);
            hasError = Boolean.parseBoolean(words[3]);
            if (hasError) {
                if (words.length < 5) {
                    throw new IllegalArgumentException("ModeData: expected length of 5 but was: " + words.length);
                }
                errorMessage = words[4];
            }
        }

        public String getModeDescription() {
            return keyWord + ":" + modeName + ":" + subModeNr + ";";
        }
    }

}
