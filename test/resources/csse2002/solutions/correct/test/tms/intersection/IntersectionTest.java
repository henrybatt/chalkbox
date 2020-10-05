package tms.intersection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tms.route.Route;
import tms.util.RouteNotFoundException;

import java.util.List;

import static org.junit.Assert.*;

public class IntersectionTest {
    private Intersection intersection1;

    @Before
    public void setUp() {
        intersection1 = new Intersection("A");
    }

    @Test
    public void getIdTest() {
        assertEquals("A", intersection1.getId());
    }

    @Test
    public void getConnectionsEmptyTest() {
        assertEquals(0, intersection1.getConnections().size());
    }

    @Test
    public void getConnectionExistsTest() {
        Intersection intersection2 = new Intersection("B");
        intersection1.addConnection(intersection2, 100);
        try {
            Route con = intersection1.getConnection(intersection2);
            Assert.assertEquals(100, con.getSpeed());
        } catch (RouteNotFoundException e) {
            fail();
        }
    }

    @Test (expected = RouteNotFoundException.class)
    public void getConnectionDoesNotExistsTest() throws RouteNotFoundException {
        Intersection intersection2 = new Intersection("B");
        intersection1.getConnection(intersection2);
    }

    @Test
    public void getConnectionsTest() {
        Intersection intersection2 = new Intersection("B");
        Intersection intersection3 = new Intersection("C");
        intersection1.addConnection(intersection2, 60);
        intersection1.addConnection(intersection3, 60);
        try {
            assertEquals(2, intersection1.getConnections().size());
            assertTrue(intersection1.getConnections().contains(
                    intersection1.getConnection(intersection2)));
            assertTrue(intersection1.getConnections().contains(
                    intersection1.getConnection(intersection3)));
        } catch (RouteNotFoundException e) {
            fail();
        }
    }

    @Test
    public void getConnectionsNonModifiableTest() {
        Intersection intersection2 = new Intersection("B");
        Intersection intersection3 = new Intersection("C");
        intersection1.addConnection(intersection2, 60);
        intersection1.addConnection(intersection3, 60);

        List<Route> routes = intersection1.getConnections();
        routes.remove(0);
        try {
            assertEquals(2, intersection1.getConnections().size());
            assertTrue(intersection1.getConnections().contains(
                    intersection1.getConnection(intersection2)));
            assertTrue(intersection1.getConnections().contains(
                    intersection1.getConnection(intersection3)));
        } catch (RouteNotFoundException e) {
            fail();
        }

        routes.add(new Route("B:A", intersection2, 60));
        routes.add(new Route("D:A", new Intersection("D"), 60));
        try {
            assertEquals(2, intersection1.getConnections().size());
            assertTrue(intersection1.getConnections().contains(
                    intersection1.getConnection(intersection2)));
            assertTrue(intersection1.getConnections().contains(
                    intersection1.getConnection(intersection3)));
        } catch (RouteNotFoundException e) {
            fail();
        }
    }

    @Test
    public void getConnectedIntersectionsEmptyListTest() {
        Assert.assertTrue(intersection1.getConnectedIntersections().isEmpty());
    }

    @Test
    public void getConnectedIntersectionsTest() {
        Intersection intersection2 = new Intersection("B");
        Intersection intersection3 = new Intersection("C");
        Intersection intersection4 = new Intersection("D");
        Intersection intersection5 = new Intersection("E");
        intersection1.addConnection(intersection2, 100);
        intersection1.addConnection(intersection3, 100);
        intersection1.addConnection(intersection4, 100);
        intersection2.addConnection(intersection4, 100);
        intersection2.addConnection(intersection5, 100); // indirect connection
        List<Intersection> connectedIntersections = intersection1.getConnectedIntersections();
        Assert.assertEquals(3, connectedIntersections.size());
        Intersection[] intersections = {intersection2, intersection3, intersection4};
        for (Intersection intersection : intersections) {
            Assert.assertTrue(connectedIntersections.contains(intersection));
        }
    }

    @Test
    public void addConnectionTest() {
        Intersection intersection2 = new Intersection("B");
        intersection1.addConnection(intersection2, 80);
        try {
            Route createdConnection = intersection1.getConnection(intersection2);
            assertEquals(intersection2, createdConnection.getFrom());
            assertEquals(80, createdConnection.getSpeed());
            assertFalse(createdConnection.hasSpeedSign());
            assertNull(createdConnection.getTrafficLight());
            assertEquals("B:A:80:0", createdConnection.toString());
        } catch (RouteNotFoundException e) {
            fail();
        }
    }

    @Test (expected = IllegalArgumentException.class)
    public void addConnectionNegativeSpeedTest() {
        Intersection intersection2 = new Intersection("B");
        intersection1.addConnection(intersection2, -30);
    }

    @Test
    public void addConnectionZeroSpeedTest() {
        Intersection intersection2 = new Intersection("B");
        try {
            intersection1.addConnection(intersection2, 0);
        } catch (IllegalArgumentException e) {
            fail();
        }
        // Connection should be added successfully
        try {
            assertEquals(0, intersection1.getConnection(intersection2).getSpeed());
        } catch (RouteNotFoundException e) {
            fail();
        }
    }

    @Test
    public void addConnectionConnectionDuplicateTest() {
        Intersection intersection2 = new Intersection("B");
        intersection1.addConnection(intersection2, 100);
        try {
            intersection1.addConnection(intersection2, 100);
            fail();
        } catch (IllegalStateException expected) {}

        Assert.assertEquals(1, intersection1.getConnectedIntersections().size());
    }

    @Test
    public void addConnectionConnectionDuplicateDifferentSpeedsTest() {
        Intersection intersection2 = new Intersection("B");
        intersection1.addConnection(intersection2, 100);
        try {
            intersection1.addConnection(intersection2, 80);
            fail();
        } catch (IllegalStateException expected) {}

        Assert.assertEquals(1, intersection1.getConnectedIntersections().size());
    }

    @Test
    public void reduceIncomingSpeedSignNoSignTest() {
        Intersection intersection2 = new Intersection("B");
        Intersection intersection3 = new Intersection("C");
        intersection1.addConnection(intersection2, 100);
        intersection1.addConnection(intersection3, 100);
        intersection1.reduceIncomingSpeedSigns();
        try {
            Assert.assertEquals(100, intersection1.getConnection(intersection2).getSpeed());
        } catch (RouteNotFoundException e) {
            fail();
        }
    }

    @Test
    public void reduceIncomingSpeedSignTwoSignsTest() {
        Intersection intersection2 = new Intersection("B");
        Intersection intersection3 = new Intersection("C");
        Intersection intersection4 = new Intersection("D");
        intersection1.addConnection(intersection2, 100);
        intersection1.addConnection(intersection3, 100);
        intersection1.addConnection(intersection4, 100);
        try {
            intersection1.getConnection(intersection2).addSpeedSign(80);
            intersection1.getConnection(intersection3).addSpeedSign(100);
            intersection1.reduceIncomingSpeedSigns();
            Assert.assertEquals(70, intersection1.getConnection(intersection2).getSpeed());
            Assert.assertEquals(90, intersection1.getConnection(intersection3).getSpeed());
            Assert.assertEquals(100, intersection1.getConnection(intersection4).getSpeed());
            assertFalse(intersection1.getConnection(intersection4).hasSpeedSign());
        } catch (RouteNotFoundException e) {
            fail();
        }
    }

    @Test
    public void reduceIncomingSpeedSignMinCapAt50Test() {
        Intersection intersection2 = new Intersection("B");
        Intersection intersection3 = new Intersection("C");
        Intersection intersection4 = new Intersection("D");
        intersection1.addConnection(intersection2, 100);
        intersection1.addConnection(intersection3, 100);
        intersection1.addConnection(intersection4, 100);
        try {
            intersection1.getConnection(intersection2).addSpeedSign(50);
            intersection1.getConnection(intersection3).addSpeedSign(65);
            intersection1.getConnection(intersection4).addSpeedSign(54);
            intersection1.reduceIncomingSpeedSigns();
            assertEquals(50, intersection1.getConnection(intersection2).getSpeed());
            assertEquals(55, intersection1.getConnection(intersection3).getSpeed());
            assertEquals(50, intersection1.getConnection(intersection4).getSpeed());
            intersection1.reduceIncomingSpeedSigns();
            assertEquals(50, intersection1.getConnection(intersection2).getSpeed());
            assertEquals(50, intersection1.getConnection(intersection3).getSpeed());
            assertEquals(50, intersection1.getConnection(intersection4).getSpeed());
        } catch (RouteNotFoundException e) {
            fail();
        }
    }

    @Test
    public void reduceIncomingSpeedSignMinStartBelow50Test() {
        Intersection intersection2 = new Intersection("B");
        intersection1.addConnection(intersection2, 100);
        try {
            intersection1.getConnection(intersection2).addSpeedSign(30);
            intersection1.reduceIncomingSpeedSigns();
            Assert.assertEquals(30, intersection1.getConnection(intersection2).getSpeed());
        } catch (RouteNotFoundException e) {
            fail();
        }
    }

    @Test
    @Deprecated
    public void toStringTest() {
        assertEquals("A", intersection1.toString());
    }
}
