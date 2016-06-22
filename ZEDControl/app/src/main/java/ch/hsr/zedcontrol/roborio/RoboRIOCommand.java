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

    DRIVE_FREE("Mode:M_Drive:0;"),

    LIFT_FRONT_WHEELS("Mode:M_Stairs:0;"),
    LIFT_REAR_WHEELS("Mode:M_Stairs:1;"),
    DRIVE_FALL_PROTECTION("Mode:M_Stairs:2;"),
    LOWER_FRONT_WHEELS("Mode:M_Stairs:3;"),
    LOWER_REAR_WHEELS("Mode:M_Stairs:4;");

    private final String command;

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
     * Returns true if a given string describes this commands.
     *
     * @param otherCommand The string that describes the command you want to check if it is equal.
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
