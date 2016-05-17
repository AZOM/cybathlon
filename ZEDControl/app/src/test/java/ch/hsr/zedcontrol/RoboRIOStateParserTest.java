package ch.hsr.zedcontrol;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import ch.hsr.zedcontrol.roborio.RoboRIOLockException;
import ch.hsr.zedcontrol.roborio.RoboRIOModeException;
import ch.hsr.zedcontrol.roborio.RoboRIOModes;
import ch.hsr.zedcontrol.roborio.RoboRIOStateException;
import ch.hsr.zedcontrol.roborio.parsing.RoboRIOStateParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class RoboRIOStateParserTest {

    private RoboRIOStateParser _parser;

    @Before
    public void setUp() {
        _parser = new RoboRIOStateParser();
    }

    @Test
    public void parse_LockInfoNoError_returnsLockString() throws RoboRIOStateException, RoboRIOModeException, RoboRIOLockException {
        // Arrange
        String testData = "Lock:false;";

        // Act
        ArrayList<String> response = _parser.parse(testData);

        // Assert
        assertEquals(RoboRIOModes.LOCK.toString(), response.get(0));
    }

    @Test
    public void parse_LockInfoWithError_throwsRoboRIOLockException() throws RoboRIOStateException, RoboRIOModeException {
        // Arrange
        String testData = "Lock:true:Already locked by foo;";

        // Act
        try {
            _parser.parse(testData);
            fail("Should have thrown RoboRIOLockException");
        } catch (RoboRIOLockException e) {
            // Assert
            assertEquals(e.getMessage(), "Already locked by foo");
        }
    }

    @Test
    public void parse_UnlockInfoNoError_returnsUnlockString() throws RoboRIOStateException, RoboRIOModeException, RoboRIOLockException {
        // Arrange
        String testData = "Unlock:false;";

        // Act
        ArrayList<String> response = _parser.parse(testData);

        // Assert
        assertEquals(RoboRIOModes.UNLOCK.toString(), response.get(0));
    }

    @Test
    public void parse_UnlockInfoWithError_throwsRoboRIOLockException() throws RoboRIOStateException, RoboRIOModeException {
        // Arrange
        String testData = "Unlock:true:Can this ever happen?!;";

        // Act
        try {
            _parser.parse(testData);
            fail("Should have thrown RoboRIOLockException");
        } catch (RoboRIOLockException e) {
            // Assert
            assertEquals(e.getMessage(), "Can this ever happen?!");
        }
    }

    @Test
    public void parse_modeStartUpNoError_returnsStartUpString() throws RoboRIOStateException, RoboRIOModeException, RoboRIOLockException {
        // Arrange
        String testData = "Mode:M_StartUp:0:false:;";

        // Act
        ArrayList<String> response = _parser.parse(testData);

        // Assert
        assertEquals(RoboRIOModes.START_UP.toString(), response.get(0));
    }

    @Test
    public void parse_modeStartUpWithError_throwsRoboRIOModeException() throws RoboRIOStateException, RoboRIOLockException {
        // Arrange
        String testData = "Mode:M_StartUp:0:true:This is my error message;";

        // Act
        try {
            _parser.parse(testData);
            Assert.fail("Should have thrown RoboRIOModeException");
        } catch (RoboRIOModeException e) {

            // Assert
            assertEquals("This is my error message", e.getMessage());
        }
    }

    @Test
    public void parse_batteryStatusInfo_returnsVoltageString() throws RoboRIOStateException, RoboRIOModeException, RoboRIOLockException {
        // Arrange
        String testData = "Battery:12.34;";

        // Act
        ArrayList<String> response = _parser.parse(testData);

        // Assert
        assertEquals("12.34 V", response.get(0));
    }

    @Test
    public void parse_stateNoneNoError_returnsStateString() throws RoboRIOStateException, RoboRIOModeException, RoboRIOLockException {
        // Arrange
        String testData = "State::0:false:;";

        // Act
        ArrayList<String> response = _parser.parse(testData);

        // Assert
        assertEquals("State::0;", response.get(0));
    }

    @Test
    public void parse_stateNoneWithError_throwsRoboRIOStateException() throws RoboRIOModeException, RoboRIOLockException {
        // Arrange
        String testData = "State::0:true:This is my error message;";

        // Act
        try {
            _parser.parse(testData);
            Assert.fail("Should have thrown RoboRIOStateException");
        } catch (RoboRIOStateException e) {

            // Assert
            assertEquals("This is my error message", e.getMessage());
        }
    }

    @Test
    public void parse_lineWithTwoCommands_returnsTwoValidResults() throws RoboRIOLockException, RoboRIOModeException, RoboRIOStateException {
        // Arrange
        String testData = "Lock:false:;State::0:false:;";

        // Act
        ArrayList<String> results = _parser.parse(testData);

        // Assert
        assertEquals(2, results.size());
        assertEquals(RoboRIOModes.LOCK.toString(), results.get(0));
        assertEquals("State::0;", results.get(1));
    }

    @Test
    public void parse_commandSplitIntoTwoLines_returnsValidResultOnSecondCall() throws RoboRIOLockException, RoboRIOModeException, RoboRIOStateException {
        // Arrange
        String part1 = "Lo";
        String part2 = "ck:false:;";

        // Act
        _parser.parse(part1);
        ArrayList<String> results = _parser.parse(part2);

        // Assert
        assertEquals(RoboRIOModes.LOCK.toString(), results.get(0));
    }


}
