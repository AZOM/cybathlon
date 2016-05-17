package ch.hsr.zedcontrol;

import org.junit.Test;

import ch.hsr.zedcontrol.roborio.RoboRIOModes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class RoboRIOModesTest {

    @Test
    public void equalsCommand_validOtherCommand_returnsTrue() {
        // Arrange
        String otherCommand = "Mode:M_StartUp:0;";
        RoboRIOModes mode = RoboRIOModes.START_UP;

        // Act
        boolean result = mode.equalsCommand(otherCommand);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsCommand_invalidOtherCommand_returnsFalse() {
        // Arrange
        String otherCommand = "does not exist";
        RoboRIOModes mode = RoboRIOModes.START_UP;

        // Act
        boolean result = mode.equalsCommand(otherCommand);

        // Assert
        assertFalse(result);
    }

    @Test
    public void equalsCommand_null_returnsFalse() {
        // Arrange
        RoboRIOModes mode = RoboRIOModes.START_UP;

        // Act
        boolean result = mode.equalsCommand(null);

        // Assert
        assertFalse(result);
    }

    @Test
    public void getModeFromStringDescription_validString_returnsEnumValue() {
        // Arrange
        String testString = "Mode:M_StartUp:0;";

        // Act
        RoboRIOModes mode = RoboRIOModes.getModeFromStringDescription(testString);

        // Assert
        assertEquals(RoboRIOModes.START_UP, mode);
    }

    @Test
    public void getModeFromStringDescription_invalidString_returnsNull() {
        // Arrange
        String testString = "invalid";

        // Act
        RoboRIOModes mode = RoboRIOModes.getModeFromStringDescription(testString);

        // Assert
        assertEquals(null, mode);
    }


}
