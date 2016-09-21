package ch.hsr.zedcontrol.roborio;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class RoboRIOStateTest {

    @Test
    public void equalsState_NoMode_returnsTrue() {
        // Arrange
        String otherState = "State:NoMode:0;";
        RoboRIOState state = RoboRIOState.NO_MODE;

        // Act
        boolean result = state.equalsState(otherState);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsState_EmergencyStop_returnsTrue() {
        // Arrange
        String otherState = "State:M_EmergencyStop:0;";
        RoboRIOState state = RoboRIOState.EMERGENCY_STOP;

        // Act
        boolean result = state.equalsState(otherState);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsState_PowerOff_returnsTrue() {
        // Arrange
        String otherState = "State:M_PowerOff:0;";
        RoboRIOState state = RoboRIOState.POWER_OFF;

        // Act
        boolean result = state.equalsState(otherState);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsState_StartUp_returnsTrue() {
        // Arrange
        String otherState = "State:M_StartUp:0;";
        RoboRIOState state = RoboRIOState.START_UP;

        // Act
        boolean result = state.equalsState(otherState);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsState_DriveFree_returnsTrue() {
        // Arrange
        String otherState = "State:M_Drive:4;";
        RoboRIOState state = RoboRIOState.DRIVE_FREE;

        // Act
        boolean result = state.equalsState(otherState);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsState_LiftFrontWheels_returnsTrue() {
        // Arrange
        String otherState = "State:M_Stairs:0;";
        RoboRIOState state = RoboRIOState.LIFT_FRONT_WHEELS;

        // Act
        boolean result = state.equalsState(otherState);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsState_LiftRearWheels_returnsTrue() {
        // Arrange
        String otherState = "State:M_Stairs:1;";
        RoboRIOState state = RoboRIOState.LIFT_REAR_WHEELS;

        // Act
        boolean result = state.equalsState(otherState);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsState_DriveFallProtection_returnsTrue() {
        // Arrange
        String otherState = "State:M_Stairs:2;";
        RoboRIOState state = RoboRIOState.DRIVE_FALL_PROTECTION;

        // Act
        boolean result = state.equalsState(otherState);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsState_LowerFrontWheels_returnsTrue() {
        // Arrange
        String otherState = "State:M_Stairs:3;";
        RoboRIOState state = RoboRIOState.LOWER_FRONT_WHEELS;

        // Act
        boolean result = state.equalsState(otherState);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsState_LowerRearWheels_returnsTrue() {
        // Arrange
        String otherState = "State:M_Stairs:4;";
        RoboRIOState state = RoboRIOState.LOWER_REAR_WHEELS;

        // Act
        boolean result = state.equalsState(otherState);

        // Assert
        assertTrue(result);
    }

    @Test
    public void equalsState_invalidOtherState_returnsFalse() {
        // Arrange
        String otherState = "does not exist";
        RoboRIOState state = RoboRIOState.NO_MODE;

        // Act
        boolean result = state.equalsState(otherState);

        // Assert
        assertFalse(result);
    }

    @Test
    public void equalsState_null_returnsFalse() {
        // Arrange
        RoboRIOState state = RoboRIOState.START_UP;

        // Act
        boolean result = state.equalsState(null);

        // Assert
        assertFalse(result);
    }

    @Test
    public void getStateFromStringDescription_NoMode_returnsEnumValue() {
        // Arrange
        String testString = "State:NoMode:0;";

        // Act
        RoboRIOState state = RoboRIOState.getStateFromStringDescription(testString);

        // Assert
        assertEquals(RoboRIOState.NO_MODE, state);
    }

    @Test
    public void getStateFromStringDescription_StartUp_returnsEnumValue() {
        // Arrange
        String testString = "State:M_StartUp:0;";

        // Act
        RoboRIOState state = RoboRIOState.getStateFromStringDescription(testString);

        // Assert
        assertEquals(RoboRIOState.START_UP, state);
    }

    @Test
    public void getStateFromStringDescription_invalidString_returnsNull() {
        // Arrange
        String testString = "invalid";

        // Act
        RoboRIOState state = RoboRIOState.getStateFromStringDescription(testString);

        // Assert
        assertEquals(null, state);
    }


}
