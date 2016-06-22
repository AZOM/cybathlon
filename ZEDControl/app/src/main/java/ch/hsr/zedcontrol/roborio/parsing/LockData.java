package ch.hsr.zedcontrol.roborio.parsing;

/**
 * Represents data from the serial bus that is retrieved when a Lock/Unlock has happened.
 */
public class LockData implements ParserData {
    private final String keyWord;
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
            errorMessage = words[2].replace(";", "");
        }
    }


    @Override
    public KeyWords getKeyWord() {
        if (keyWord.equals(KeyWords.LOCK.toString())) {
            return KeyWords.LOCK;
        } else {
            return KeyWords.UNLOCK;
        }
    }


    @Override
    public String getDescription() {
        return keyWord + ";";
    }

}
