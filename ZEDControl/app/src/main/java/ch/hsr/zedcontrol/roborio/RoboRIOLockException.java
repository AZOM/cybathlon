package ch.hsr.zedcontrol.roborio;

/**
 * Thrown when LOCK or UNLOCK caused error.
 */
public class RoboRIOLockException extends Throwable {
    public RoboRIOLockException(String s) {
        super(s);
    }
}
