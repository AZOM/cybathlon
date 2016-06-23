package ch.hsr.zedcontrol.roborio.parsing;

/**
 * Represents a RoboRIO command as it can be read back from the serial bus.
 */
public abstract class CommandData implements ParserData {
    private final String keyWord;
    protected final String modeName;
    protected final int subModeNr;
    protected final boolean hasError;
    protected String errorMessage;

    protected CommandData(String[] words) {
        if (words.length < 4) {
            throw new IllegalArgumentException(
                    getClass().getSimpleName() + ": expected at least length of 4 but was: " + words.length);
        }

        keyWord = words[0];
        modeName = words[1];
        subModeNr = Integer.parseInt(words[2]);
        hasError = Boolean.parseBoolean(words[3]);
        if (hasError) {
            if (words.length < 5) {
                throw new IllegalArgumentException(
                        getClass().getSimpleName() + ": expected length of 5 but was: " + words.length);
            }
            errorMessage = words[4].replace(";", "");
        }
    }

    @Override
    public abstract KeyWords getKeyWord();


    @Override
    public String getDescription() {
        return keyWord + ":" + modeName + ":" + subModeNr + ";";
    }


    @Override
    public String toString() {
        return keyWord + ":" + modeName + ":" + subModeNr + ":" + hasError + ":" + errorMessage + ";";
    }
}
