package ch.hsr.zedcontrol.roborio;

/**
 * Contains all commands (String) that can be sent to the roboRIO peripheral.
 */
public enum RoboRIOModes {
    NO_MODE(":0;"),

    POWER_OFF("M_PowerOff:0;"),
    POWER_ON("M_StartUp:0;"),

    DRIVE_FREE("M_Drive:0;"),

    LIFT_FRONT_WHEELS("M_Stairs:0;"),
    LIFT_REAR_WHEELS("M_Stairs:1;"),
    DRIVE_FALL_PROTECTION("M_Stairs:2;"),
    LOWER_FRONT_WHEELS("M_Stairs:3;"),
    LOWER_REAR_WHEELS("M_Stairs:4;");

    private final String command;

    RoboRIOModes(String s) {
        command = s;
    }

    public boolean equalsCommand(String otherCommand) {
        return otherCommand != null && command.equals(otherCommand);
    }

    public String toString() {
        return this.command;
    }
}
