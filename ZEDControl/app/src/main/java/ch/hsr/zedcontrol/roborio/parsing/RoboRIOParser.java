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
public class RoboRIOParser {

    private static final String TAG = RoboRIOParser.class.getSimpleName();

    private StringBuilder _lineBuffer = new StringBuilder();


    /**
     * Takes a string as input and tries to interpret it as a valid answer from the serial-bus. Every line shall end in
     * ';'.
     *
     * @param data The input data.
     * @return An ArrayList of successfully (complete) parsed lines.
     * @throws RoboRIOLockException
     * @throws RoboRIOStateException
     * @throws RoboRIOModeException
     */
    public ArrayList<String> parse(@NonNull String data) throws RoboRIOLockException, RoboRIOStateException, RoboRIOModeException {
        ArrayList<String> result = new ArrayList<>();

        // Split input into separate lines and keep the separator!
        // data can contain multiple complete lines (ended by ';') like : Lock:false:;State::0:false:;
        // or incomplete line like: Lo
        // or: ock:false:;
        final String[] lines = data.split("(?<=;)");

        for (String line : lines) {

            if (_lineBuffer.toString().contains(";")) {
                final String parsed = handleLineComplete();
                if (parsed != null) {
                    result.add(parsed);
                }
            } else {
                // line is incomplete -> add to _lineBuffer
                _lineBuffer.append(line);

                // We have to check again, if the line is complete now to be able to process it.
                if (_lineBuffer.toString().contains(";")) {
                    final String parsed = handleLineComplete();
                    if (parsed != null) {
                        result.add(parsed);
                    }
                }
            }

        }

        return result;
    }


    private String handleLineComplete() throws RoboRIOStateException, RoboRIOLockException, RoboRIOModeException {
        final String parsedLine = parseLine(_lineBuffer.toString());

        // reset the _lineBuffer, since a complete line has been written
        _lineBuffer.setLength(0);

        return parsedLine;
    }


    @Nullable
    private static String parseLine(@NonNull String line) throws RoboRIOStateException, RoboRIOLockException, RoboRIOModeException {
        final String[] words = line.split(":");

        switch (words[0]) {
            case "Battery":
                return parseBatteryData(words);

            case "State":
                return parseStateData(words);

            case "Lock":
            case "Unlock":
                return parseLockData(words);

            case "Mode":
                return parseModeData(words);

            default:
                // command unknown, or incomplete -> handled by internal buffer
                Log.wtf(TAG, "parseLine() -> command unknown, or incomplete: " + words[0]);
                return null;
        }
    }


    private static String parseBatteryData(String[] words) {
        final BatteryData batteryData = new BatteryData(words);

        return batteryData.getDescription();
    }


    // Used because we want a different Exception here - else it is the same as parseModeData
    private static String parseStateData(String[] words) throws RoboRIOStateException {
        final ModeData stateData = new ModeData(words);

        if (stateData.hasError) {
            throw new RoboRIOStateException(stateData.errorMessage);
        }

        return stateData.getDescription();
    }


    private static String parseLockData(String[] words) throws RoboRIOLockException {
        final LockData lockData = new LockData(words);

        if (lockData.hasError) {
            throw new RoboRIOLockException(lockData.errorMessage);
        }

        return lockData.getDescription();
    }


    private static String parseModeData(String[] words) throws RoboRIOModeException {
        final ModeData modeData = new ModeData(words);

        if (modeData.hasError) {
            throw new RoboRIOModeException(modeData.errorMessage);
        }

        return modeData.getDescription();
    }


}
