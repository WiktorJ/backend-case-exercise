package exercise.utils;

import exercise.domain.LogEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {

    @Test
    public void shouldReutrnMalformedLogEntry() {
        LogEntry incorrectLog = Utils.createDaoFromLog("Incorrect log");
        assertTrue(incorrectLog.isMalformed());
    }
}
