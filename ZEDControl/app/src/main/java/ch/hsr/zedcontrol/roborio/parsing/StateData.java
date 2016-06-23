package ch.hsr.zedcontrol.roborio.parsing;

/**
 * Represents data from the serial bus that is retrieved when a state is reached - means a command (as defined in
 * {@link ch.hsr.zedcontrol.roborio.RoboRIOCommand}) has been successfully executed. The last state will continuously
 * be sent as keep alive signal.
 */
public class StateData extends CommandData {

    public StateData(String[] words) {
        super(words);
    }

    @Override
    public KeyWords getKeyWord() {
        return KeyWords.STATE;
    }
}
