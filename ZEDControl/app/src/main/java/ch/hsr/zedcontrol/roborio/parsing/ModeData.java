package ch.hsr.zedcontrol.roborio.parsing;

/**
 * Represents data from the serial bus that is retrieved directly after a Mode
 * (see: {@link ch.hsr.zedcontrol.roborio.RoboRIOModes}) is acknowledged.
 */
public class ModeData implements ParserData {
    private final String keyWord;
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
            errorMessage = words[4].replace(";", "");
        }
    }


    @Override
    public KeyWords getKeyWord() {
        return KeyWords.MODE;
    }


    @Override
    public String getDescription() {
        return keyWord + ":" + modeName + ":" + subModeNr + ";";
    }
}
