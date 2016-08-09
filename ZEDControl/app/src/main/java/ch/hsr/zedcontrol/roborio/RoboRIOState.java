package ch.hsr.zedcontrol.roborio;

import org.jetbrains.annotations.Contract;

/**
 * Defines all valid states a RoboRIO can have.
 */
public enum RoboRIOState {
    NO_MODE("State:NoMode:0;"),

    EMERGENCY_STOP("State:M_EmergencyStop:0;"),

    POWER_OFF("State:M_PowerOff:0;"),

    START_UP("State:M_StartUp:0;"),

    DRIVE_THROTTLED("State:M_Drive:0;"),
    DRIVE_THROTTLED_STEERING_REAR("State:M_Drive:1;"),
    DRIVE_THROTTLED_STEERING_BOTH("State:M_Drive:2;"),
    DRIVE_THROTTLED_STEERING_BOTH_MIRRORED("State:M_Drive:3;"),
    DRIVE_FREE("State:M_Drive:4;"),

    LIFT_FRONT_WHEELS("State:M_Stairs:0;"),
    LIFT_REAR_WHEELS("State:M_Stairs:1;"),
    DRIVE_FALL_PROTECTION("State:M_Stairs:2;"),
    LOWER_FRONT_WHEELS("State:M_Stairs:3;"),
    LOWER_REAR_WHEELS("State:M_Stairs:4;");

    private final String state;

    /**
     * Constructor
     *
     * @param s The string that describes this RoboRIOState's state.
     */
    RoboRIOState(String s) {
        state = s;
    }


    /**
     * Get an enum value from string.
     *
     * @param stateString The string that shall be converted to enum value.
     * @return RoboRioModes enum value or null, if string did not match any enum value.
     */
    public static RoboRIOState getStateFromStringDescription(String stateString) {
        for (RoboRIOState state : RoboRIOState.values()) {
            if (state.equalsState(stateString)) {
                return state;
            }
        }

        return null;
    }


    /**
     * Compares a given string with the current RoboRIOState object.
     *
     * @param otherStateString the string that may describe the current RoboRIOState object
     * @return True if the given string describes the current RoboRIOState object
     */
    @Contract("null -> false")
    public boolean equalsState(String otherStateString) {
        return otherStateString != null && state.equals(otherStateString);
    }


    @Contract(pure = true)
    public String toString() {
        return this.state;
    }
}
