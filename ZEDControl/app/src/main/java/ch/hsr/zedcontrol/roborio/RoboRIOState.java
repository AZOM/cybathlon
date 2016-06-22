package ch.hsr.zedcontrol.roborio;

import org.jetbrains.annotations.Contract;

/**
 * Contains all valid states (String) a RoboRIO can have.
 */
public enum RoboRIOState {
    NO_MODE("State:NoMode:0;"),

    EMERGENCY_STOP("State:M_EmergencyStop:0;"),

    POWER_OFF("State:M_PowerOff:0;"),

    START_UP("State:M_StartUp:0;"),

    DRIVE_FREE("State:M_Drive:0;"),

    LIFT_FRONT_WHEELS("State:M_Stairs:0;"),
    LIFT_REAR_WHEELS("State:M_Stairs:1;"),
    DRIVE_FALL_PROTECTION("State:M_Stairs:2;"),
    LOWER_FRONT_WHEELS("State:M_Stairs:3;"),
    LOWER_REAR_WHEELS("State:M_Stairs:4;");

    private final String state;

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


    @Contract("null -> false")
    public boolean equalsState(String otherStateString) {
        return otherStateString != null && state.equals(otherStateString);
    }


    @Contract(pure = true)
    public String toString() {
        return this.state;
    }
}
