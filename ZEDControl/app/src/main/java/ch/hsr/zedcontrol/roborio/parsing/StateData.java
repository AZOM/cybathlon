package ch.hsr.zedcontrol.roborio.parsing;

/**
 * Represents data from the serial bus that is retrieved when a State is posted (keep alive signal).
 * It looks the same as {@link ModeData} but we want to keep things clear and separated.
 */
public class StateData extends ModeData {

    public StateData(String[] words) {
        super(words);
    }

    @Override
    public KeyWords getKeyWord() {
        return KeyWords.STATE;
    }
}
