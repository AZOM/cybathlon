package ch.hsr.zedcontrol.roborio.parsing;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

import ch.hsr.zedcontrol.roborio.RoboRIOLockException;
import ch.hsr.zedcontrol.roborio.RoboRIOModeException;
import ch.hsr.zedcontrol.roborio.RoboRIOStateException;

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


}
