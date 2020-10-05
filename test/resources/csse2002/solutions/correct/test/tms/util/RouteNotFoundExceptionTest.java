package tms.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class RouteNotFoundExceptionTest {
    private RouteNotFoundException e;

    @Test
    public void RouteNotFoundExceptionSuperCreateTest() {
        e = new RouteNotFoundException();
    }

//    @Test
//    public void RouteNotFoundExceptionMsgCreateTest() {
//        e = new RouteNotFoundException("message");
//        assertEquals("message", e.getMessage());
//    }
//
//    @Test
//    public void RouteNotFoundExceptionErrCreateTest() {
//        Throwable cause = new Error();
//        e = new RouteNotFoundException("message", cause);
//        assertEquals(cause, e.getCause());
//    }
}
