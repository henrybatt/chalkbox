package tms.util;

import org.junit.Test;

public class DuplicateSensorExceptionTest {
    private DuplicateSensorException e;

    @Test
    public void DuplicateSensorSuperCreateTest() {
        e = new DuplicateSensorException();
    }

    // Implementing additional constructors is optional

//    @Test
//    public void DuplicateSensorMsgCreateTest() {
//        e = new DuplicateSensorException("message");
//        assertEquals("message", e.getMessage());
//    }
//
//    @Test
//    public void DuplicateSensorErrCreateTest() {
//        Throwable cause = new Error();
//        e = new DuplicateSensorException("message", cause);
//        assertEquals(cause, e.getCause());
//    }

}
