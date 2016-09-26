package ch.hsr.zedcontrol.roborio;

import org.jetbrains.annotations.Contract;

/**
 * Contains all commands (String) that can be sent to the RoboRIO peripheral.
 */
public enum RoboRIOCommand {
    LOCK("Lock;"),
    UNLOCK("Unlock;"),

    NO_MODE("Mode:NoMode:0;"),

    POWER_OFF("Mode:M_PowerOff:0;"),

    START_UP("Mode:M_StartUp:0;"),

    DRIVE_THROTTLED("Mode:M_Drive:0;"),
    DRIVE_THROTTLED_STEERING_REAR("Mode:M_Drive:1;"),
    DRIVE_THROTTLED_STEERING_BOTH("Mode:M_Drive:2;"),
    DRIVE_THROTTLED_STEERING_BOTH_MIRRORED("Mode:M_Drive:3;"),
    DRIVE_FREE("Mode:M_Drive:4;"),

    LIFT_FRONT_WHEELS("Mode:M_Stairs:0;"),
    LIFT_REAR_WHEELS("Mode:M_Stairs:1;"),
    DRIVE_FALL_PROTECTION("Mode:M_Stairs:2;"),
    LOWER_FRONT_WHEELS("Mode:M_Stairs:3;"),
    LOWER_REAR_WHEELS("Mode:M_Stairs:4;"),
    DRIVE_FIXED_STEERING("Mode:M_Stairs:5;");

    private final String command;


    /**
     * Constructor
     *
     * @param s The string that describes this RoboRIOCommand's command.
     */
    RoboRIOCommand(String s) {
        command = s;
    }


    /**
     * Get an enum value from string.
     *
     * @param modeString The string that shall be converted to enum value.
     * @return RoboRioCommands enum value or null, if string did not match any enum value.
     */
    public static RoboRIOCommand getCommandFromStringDescription(String modeString) {
        for (RoboRIOCommand command : RoboRIOCommand.values()) {
            if (command.equalsCommand(modeString)) {
                return command;
            }
        }

        return null;
    }


    /**
     * Compares a given string with the current RoboRIOCommand object.
     *
     * @param otherCommand the string that may describe the current RoboRIOCommand object
     * @return True if the given string describes the current RoboRIOCommand object
     */
    @Contract("null -> false")
    public boolean equalsCommand(String otherCommand) {
        return otherCommand != null && command.equals(otherCommand);
    }


    @Contract(pure = true)
    public String toString() {
        return this.command;
    }
}
