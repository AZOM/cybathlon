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
            String parsedLine = parseLine(line);

            if (parsedLine == null) {
                Log.wtf(TAG, "parse() -> could not parse line for: " + line);
            } else {
                result.add(parsedLine);
            }
        }

        return result;
    }

    @Nullable
    private static String parseLine(@NonNull String line) throws RoboRIOStateException, RoboRIOLockException, RoboRIOModeException {
        if (line.startsWith("Battery:")) {
            return new BatteryData(line).voltage + " V";
        }

        if (line.startsWith("State:")) {
            final StateData stateData = new StateData(line);

            if (stateData.hasError) {
                throw new RoboRIOStateException(stateData.errorMessage);
            }

            return stateData.modeName;
        }

        if (line.startsWith("Lock:") || line.startsWith("Unlock:")) {
            final LockData lockData = new LockData(line);

            if (lockData.hasError) {
                throw new RoboRIOLockException(lockData.errorMessage);
            }

            return lockData.getLockDescription();
        }

        if (line.startsWith("Mode:")) {
            final ModeData modeData = new ModeData(line);

            if (modeData.hasError) {
                throw new RoboRIOModeException(modeData.errorMessage);
            }

            return modeData.getModeDescription();
        }

        // unknown
        return null;
    }

    private static class BatteryData {
        protected final String keyWord;
        protected final double voltage;

        public BatteryData(String data) {
            // split into separate substrings (format is like: Battery:12.34;)
            final String[] strings = data.split(":");

            keyWord = strings[0];
            voltage = Double.parseDouble(strings[1]);
        }
    }

    private static class StateData {
        protected final String keyWord;
        protected final String modeName;
        protected final int subModeNr;
        protected final boolean hasError;
        protected String errorMessage;

        public StateData(String data) {
            // split into separate substrings (format is like: State:M_StartUp:0:false:;)
            final String[] strings = data.split(":");

            keyWord = strings[0];
            modeName = strings[1];
            subModeNr = Integer.parseInt(strings[2]);
            hasError = Boolean.parseBoolean(strings[3]);
            if (hasError) {
                errorMessage = strings[4];
            }
        }
    }

    private static class LockData {
        protected final String keyWord;
        protected final boolean hasError;
        protected String errorMessage;

        public LockData(String data) {
            // split into separate substrings (format is like: Lock:true:errorMessage;)
            final String[] strings = data.split(":");

            keyWord = strings[0];
            hasError = Boolean.parseBoolean(strings[1]);
            if (hasError) {
                errorMessage = strings[2];
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

        public ModeData(String data) {
            // split into separate substrings (format is like: Mode:M_StartUp:0:true:errorMessage;)
            final String[] strings = data.split(":");

            keyWord = strings[0];
            modeName = strings[1];
            subModeNr = Integer.parseInt(strings[2]);
            hasError = Boolean.parseBoolean(strings[3]);
            if (hasError) {
                errorMessage = strings[4];
            }
        }

        public String getModeDescription() {
            return keyWord + ":" + modeName + ":" + subModeNr + ";";
        }
    }

}
