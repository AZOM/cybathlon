package ch.hsr.zedcontrol.roborio;

/**
 * Contains all commands (String) that can be sent to the roboRIO peripheral.
 */
public enum RoboRIOModes {
    LOCK("Lock;"),
    UNLOCK("Unlock;"),

    NO_MODE("Mode:0;"),

    POWER_OFF("Mode:M_PowerOff:0;"),
    POWER_ON("Mode:M_StartUp:0;"),

    DRIVE_FREE("Mode:M_Drive:0;"),

    LIFT_FRONT_WHEELS("Mode:M_Stairs:0;"),
    LIFT_REAR_WHEELS("Mode:M_Stairs:1;"),
    DRIVE_FALL_PROTECTION("Mode:M_Stairs:2;"),
    LOWER_FRONT_WHEELS("Mode:M_Stairs:3;"),
    LOWER_REAR_WHEELS("Mode:M_Stairs:4;");

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
