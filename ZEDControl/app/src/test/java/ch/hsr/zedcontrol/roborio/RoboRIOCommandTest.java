package ch.hsr.zedcontrol.roborio;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class RoboRIOCommandTest {

    @Test
    public void equalsCommand_StartUp_returnsTrue() {
        // Arrange
        String otherCommand = "Mode:M_StartUp:0;";
        RoboRIOCommand command = RoboRIOCommand.START_UP;

        // Act
        boolean result = command.equalsCommand(otherCommand);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsCommand_NoMode_returnsTrue() {
        // Arrange
        String otherCommand = "Mode:NoMode:0;";
        RoboRIOCommand command = RoboRIOCommand.NO_MODE;

        // Act
        boolean result = command.equalsCommand(otherCommand);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsCommand_invalidOtherCommand_returnsFalse() {
        // Arrange
        String otherCommand = "does not exist";
        RoboRIOCommand command = RoboRIOCommand.START_UP;

        // Act
        boolean result = command.equalsCommand(otherCommand);

        // Assert
        assertFalse(result);
    }

    @Test
    public void equalsCommand_null_returnsFalse() {
        // Arrange
        RoboRIOCommand command = RoboRIOCommand.START_UP;

        // Act
        boolean result = command.equalsCommand(null);

        // Assert
        assertFalse(result);
    }

    @Test
    public void getCommandFromStringDescription_StartUp_returnsEnumValue() {
        // Arrange
        String testString = "Mode:M_StartUp:0;";

        // Act
        RoboRIOCommand command = RoboRIOCommand.getCommandFromStringDescription(testString);

        // Assert
        assertEquals(RoboRIOCommand.START_UP, command);
    }

    @Test
    public void getCommandFromStringDescription_NoMode_returnsEnumValue() {
        // Arrange
        String testString = "Mode:NoMode:0;";

        // Act
        RoboRIOCommand command = RoboRIOCommand.getCommandFromStringDescription(testString);

        // Assert
        assertEquals(RoboRIOCommand.NO_MODE, command);
    }

    @Test
    public void getCommandFromStringDescription_invalidString_returnsNull() {
        // Arrange
        String testString = "invalid";

        // Act
        RoboRIOCommand command = RoboRIOCommand.getCommandFromStringDescription(testString);

        // Assert
        assertEquals(null, command);
    }


}
