package ch.hsr.zedcontrol.roborio;

import android.support.annotation.NonNull;

/**
 * Parses the data from UsbSerialInterface.UsbReadCallback to something meaningful.
 */
public class RoboRIOStateParser {

    private static class BatteryData {
        protected final String keyWord;
        protected final double voltage;

        public BatteryData(String data) {
            // split into separate substrings (format is like: Battery:12.34;)
            final String[] strings = data.split(":");

            keyWord = strings[0];
            voltage = Double.parseDouble(strings[1].replace(";", ""));
        }
    }

    private static class StateData {
        protected final String keyWord;
        protected final String modeName;
        protected final int subModeNr;
        protected final boolean hasError;
        protected final String errorMessage;

        public StateData(String data) {
            // split into separate substrings (format is like: State:M_StartUp:0:false:;)
            final String[] strings = data.split(":");

            keyWord = strings[0];
            modeName = strings[1];
            subModeNr = Integer.parseInt(strings[2]);
            hasError = Boolean.parseBoolean(strings[3]);
            errorMessage = strings[4].replace(";", "");
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
                errorMessage = strings[2].replace(";", "");
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
                errorMessage = strings[4].replace(";", "");
            }
        }

        public String getModeDescription() {
            return keyWord + ":" + modeName + ":" + subModeNr + ";";
        }
    }


    public static String parse(@NonNull String data) throws RoboRIOLockException, RoboRIOStateException, RoboRIOModeException {

        if (data.startsWith("Battery:")) {
            return new BatteryData(data).voltage + " V";
        }

        if (data.startsWith("State:")) {
            final StateData stateData = new StateData(data);

            if (stateData.hasError) {
                throw new RoboRIOStateException(stateData.errorMessage);
            }

            return stateData.modeName;
        }

        if (data.startsWith("Lock:") || data.startsWith("Unlock:")) {
            final LockData lockData = new LockData(data);

            if (lockData.hasError) {
                throw new RoboRIOLockException(lockData.errorMessage);
            }

            return lockData.getLockDescription();
        }

        if (data.startsWith("Mode:")) {
            final ModeData modeData = new ModeData(data);

            if (modeData.hasError) {
                throw new RoboRIOModeException(modeData.errorMessage);
            }

            return modeData.getModeDescription();
        }

        return null;
    }
}
