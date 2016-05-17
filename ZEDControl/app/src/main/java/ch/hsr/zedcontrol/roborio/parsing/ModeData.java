package ch.hsr.zedcontrol.roborio.parsing;

/**
 * Represents data from the serial bus that is retrieved when a Mode is acknowledged or a State is posted.
 */
public class ModeData implements ParserData {
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


    @Override
    public String getDescription() {
        return keyWord + ":" + modeName + ":" + subModeNr + ";";
    }
}
