package ch.hsr.zedcontrol.roborio;

/**
 * Error that is thrown when state information callback contains an error.
 */
public class RoboRIOStateException extends Exception {

    public RoboRIOStateException(String s) {
        super(s);
    }

}
