package ch.hsr.zedcontrol.roborio.parsing;

/**
 * Represents data from the serial bus that is retrieved when a battery status update is sent.
 */
public class BatteryData implements ParserData {
    protected final double voltage;
    private final String keyWord;


    public BatteryData(String[] words) {
        if (words.length < 2) {
            throw new IllegalArgumentException("BatteryData: expected length of 2 but was: " + words.length);
        }

        keyWord = words[0];
        voltage = Double.parseDouble(words[1].replace(";", ""));
    }


    @Override
    public KeyWords getKeyWord() {
        return KeyWords.BATTERY;
    }


    @Override
    public String getDescription() {
        return voltage + " V";
    }

}