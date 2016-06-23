package ch.hsr.zedcontrol.roborio.parsing;

import ch.hsr.zedcontrol.roborio.RoboRIOCommand;

/**
 * Represents data from the serial bus that is retrieved directly after a command (see: {@link RoboRIOCommand}) is
 * acknowledged.
 */
public class ModeData extends CommandData {

    public ModeData(String[] words) {
        super(words);
    }

    @Override
    public KeyWords getKeyWord() {
        return KeyWords.MODE;
    }

}
