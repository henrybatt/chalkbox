package tms.intersection;

import org.junit.*;
import tms.route.Route;
import tms.util.RouteNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class IntersectionTest {
    @Test
    public void getId() {
        String id = "TestID";
        Intersection intersection = new Intersection("TestID");
        Assert.assertEquals(id, intersection.getId());
    }
    @Test
    public void idNotNull(){
        Intersection intersection = new Intersection("TestID");
        Assert.assertNotNull(intersection.getId());
    }

    @Test
    public void getConnections() throws RouteNotFoundException {
        List<Intersection> testNetwork = new ArrayList<>();
        testNetwork.add(new Intersection("0"));
        testNetwork.add(new Intersection("1"));
        testNetwork.get(0).addConnection(testNetwork.get(1),100);
        String routeId =testNetwork.get(1).getId()+":"+testNetwork.get(0).getId();
        Route route2 = new Route(routeId,testNetwork.get(0),100);
        Assert.assertEquals(testNetwork.get(0).getConnection(testNetwork.get(1)).toString(),route2.toString());
    }

    @Test(expected = RouteNotFoundException.class)
    public void routeNotFoundExceptionTest() throws RouteNotFoundException {
        List<Intersection> testNetwork = new ArrayList<>();
        testNetwork.add(new Intersection("0"));
        testNetwork.add(new Intersection("1"));
        testNetwork.get(0).getConnection(testNetwork.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionTest() throws IllegalArgumentException {
        int speed = -1;
        List<Intersection> testNetwork = new ArrayList<>();
        testNetwork.add(new Intersection("0"));
        testNetwork.add(new Intersection("1"));
        testNetwork.get(0).addConnection(testNetwork.get(1), speed);
    }

    @Test
    public void getConnectedIntersections() {
        List<Intersection> testNetwork = new ArrayList<>();
        List<Route> connections = new ArrayList<>();
        testNetwork.add(new Intersection("0"));
        testNetwork.add(new Intersection("1"));
        testNetwork.add(new Intersection("2"));
        testNetwork.add(new Intersection("3"));
        testNetwork.get(0).addConnection(testNetwork.get(1), 100);
        testNetwork.get(0).addConnection(testNetwork.get(2), 100);
        String route1String =testNetwork.get(1).getId()+":"+testNetwork.get(0).getId();
        String route2String =testNetwork.get(2).getId()+":"+testNetwork.get(0).getId();
        connections.add(new Route(route1String,testNetwork.get(0),100));
        connections.add(new Route(route2String,testNetwork.get(0),100));
        Assert.assertEquals(connections.toString(), testNetwork.get(0).getConnections().toString());
    }

    @Test
    public void addConnection() throws RouteNotFoundException {
        List<Intersection> testNetwork = new ArrayList<>();
        int speed = 100;
        testNetwork.add(new Intersection("0"));
        testNetwork.add(new Intersection("1"));
        String routeId =testNetwork.get(1).getId()+":"+testNetwork.get(0).getId();
        Route route = new Route(routeId,testNetwork.get(0),speed);
        testNetwork.get(0).addConnection(testNetwork.get(1),speed);
        Assert.assertEquals(route.toString(),testNetwork.get(0).getConnection(testNetwork.get(1)).toString());
    }

    @Test
    public void reduceIncomingSpeedSigns() throws RouteNotFoundException {
        List<Intersection> testNetwork = new ArrayList<>();
        int speed = 100;
        testNetwork.add(new Intersection("0"));
        testNetwork.add(new Intersection("1"));
        testNetwork.get(0).addConnection(testNetwork.get(1),speed);
        testNetwork.get(0).getConnection(testNetwork.get(1)).addSpeedSign(speed);
        if(!(testNetwork.get(0).getConnection(testNetwork.get(1)).hasSpeedSign())){
            // Check if speed camera has been added
            Assert.fail();
        }else{
            testNetwork.get(0).reduceIncomingSpeedSigns();
            Assert.assertEquals(testNetwork.get(0).getConnection(testNetwork.get(1)).getSpeed(),
                    max(50, (speed - 10)));
        }
    }

    @Test
    public void getConnection() throws RouteNotFoundException {
        List<Intersection> testNetwork = new ArrayList<>();
        int speed = 100;
        testNetwork.add(new Intersection("0"));
        testNetwork.add(new Intersection("1"));
        String routeId =testNetwork.get(1).getId()+":"+testNetwork.get(0).getId();
        Route route = new Route(routeId,testNetwork.get(0),speed);
        testNetwork.get(0).addConnection(testNetwork.get(1),speed);
        Assert.assertEquals(route.toString(),testNetwork.get(0).getConnection(testNetwork.get(1)).toString());
    }

    @Test
    public void testToString() {
        String id = "TestID";
        Intersection intersection = new Intersection("TestID");
        Assert.assertEquals(id, intersection.getId());
    }
}
