package tms.route;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tms.intersection.Intersection;
import tms.sensors.DemoPressurePad;
import tms.sensors.DemoSpeedCamera;
import tms.sensors.Sensor;
import tms.util.DuplicateSensorException;

import java.util.List;

import static org.junit.Assert.*;

public class RouteTest {
    private Intersection from;
    private Route route1;
    private String id;

    @Before
    public void setup() {
        from = new Intersection("A");
        id = "A:X";
        route1 = new Route(id, from, 60);
    }

    @Test
    public void testGetFrom() {
        Assert.assertEquals(from, route1.getFrom());
    }

    @Test
    public void testGetTrafficLightNoLight() {
        assertNull(route1.getTrafficLight());
    }

    @Test
    public void testAddTrafficLight() {
        route1.addTrafficLight();
        assertNotNull(route1.getTrafficLight());
        assertEquals(TrafficSignal.RED, route1.getTrafficLight().getSignal());
    }

    @Test
    public void testSetSignalNullLight() {
        route1.setSignal(TrafficSignal.RED);
        assertNull(route1.getTrafficLight());
    }

    @Test
    public void testSetSignal() {
        route1.addTrafficLight();
        route1.setSignal(TrafficSignal.GREEN);
        assertEquals(TrafficSignal.GREEN, route1.getTrafficLight().getSignal());
    }

    @Test
    public void testAddSpeedSign() {
        route1.addSpeedSign(40);
        assertEquals(40, route1.getSpeed());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testAddSpeedSignNeg() {
        route1.addSpeedSign(-1);
    }

    @Test
    public void testAddSpeedSignZeroSpeed() {
        route1.addSpeedSign(0);
        assertEquals(0, route1.getSpeed());
    }

    @Test
    public void testAddSpeedSignOverwrites() {
        route1.addSpeedSign(90);
        route1.addSpeedSign(70);
        assertEquals(70, route1.getSpeed());
    }

    @Test
    public void testSetSpeedLimit() {
        route1.addSpeedSign(40);
        route1.setSpeedLimit(60);
        assertEquals(60, route1.getSpeed());
    }

    @Test
    public void testSetSpeedLimitNoSign() {
        try {
            route1.setSpeedLimit(300);
            fail("Route must have electronic speed sign");
        } catch (IllegalStateException expected) {}
    }

    @Test
    public void testSetSpeedLimitNegativeSpeed() {
        route1.addSpeedSign(40);
        try {
            route1.setSpeedLimit(-3);
            fail("Speed sign speed must be >= 0");
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testGetSpeedNoSign() {
        assertEquals(60, route1.getSpeed());
    }

    @Test
    public void testAddSensor() {
        int[] data = {100};
        Sensor pp = new DemoPressurePad(data, 100); //Congestion = 100
        Sensor sc = new DemoSpeedCamera(data, 100); //Congestion = 0
        try {
            route1.addSensor(sc);
            route1.addSensor(pp);
        } catch (DuplicateSensorException dse) {
            fail();
        }

    }

    @Test
    public void testAddSensorDuplicate() {
        int[] data = {100};
        Sensor pp = new DemoPressurePad(data, 100); //Congestion = 100
        Sensor sc = new DemoSpeedCamera(data, 100); //Congestion = 0
        try {
            route1.addSensor(sc);
            route1.addSensor(pp);
            // TODO: Make test more specific by using assertThrows for the next statement. Requires JUnit 4.13.
            route1.addSensor(pp);
            fail();
        } catch (DuplicateSensorException dse) {
            assertTrue(true);
        }
    }

    @Test
    public void testAddSensorDuplicateClass() {
        int[] data = {100};
        Sensor pp = new DemoPressurePad(data, 100); //Congestion = 100
        Sensor pp2 = new DemoPressurePad(data, 605); //Congestion = 0
        try {
            route1.addSensor(pp);
            // TODO: Make test more specific by using assertThrows for the next statement. Requires JUnit 4.13.
            route1.addSensor(pp2);
            fail();
        } catch (DuplicateSensorException dse) {
            assertTrue(true);
        }
    }

    @Test
    @Deprecated
    public void testToStringBasic() {
        assertEquals(id + ":60:0", route1.toString());
    }

    @Test
    @Deprecated
    public void testToStringSpeedSign() {
        route1.addSpeedSign(50);
        assertEquals(id + ":60:0:50", route1.toString());
        route1.setSpeedLimit(80);
        assertEquals(id + ":60:0:80", route1.toString());
        route1.setSpeedLimit(30);
        assertEquals(id + ":60:0:30", route1.toString());
    }

    @Test
    public void testToStringOneSensor() {
        try {
            route1.addSensor(new DemoSpeedCamera(new int[] {60}, 60));
        } catch (DuplicateSensorException ignored) {}
        assertEquals(id + ":60:1" + System.lineSeparator() + "SC:60:60",
                route1.toString());
    }

    @Test
    public void testToStringOneSensorSpeedSign() {
        route1.addSpeedSign(70);
        try {
            route1.addSensor(new DemoSpeedCamera(new int[] {60}, 60));
        } catch (DuplicateSensorException ignored) {}
        assertEquals(id + ":60:1:70" + System.lineSeparator() + "SC:60:60",
                route1.toString());
    }

    @Test
    public void testToStringTwoSensors() {
        try {
            route1.addSensor(new DemoSpeedCamera(new int[] {60}, 60));
            route1.addSensor(new DemoPressurePad(new int[] {1, 10, 2}, 5));
        } catch (DuplicateSensorException ignored) {}
        assertEquals(id + ":60:2" + System.lineSeparator()
                        + "PP:5:1,10,2" + System.lineSeparator()
                        + "SC:60:60",
                route1.toString());
    }

    @Test
    public void testToStringTwoSensorsSpeedSign() {
        route1.addSpeedSign(90);
        try {
            route1.addSensor(new DemoSpeedCamera(new int[] {60}, 60));
            route1.addSensor(new DemoPressurePad(new int[] {1, 10, 2}, 5));
        } catch (DuplicateSensorException ignored) {}
        assertEquals(id + ":60:2:90" + System.lineSeparator()
                        + "PP:5:1,10,2" + System.lineSeparator()
                        + "SC:60:60",
                route1.toString());
    }

    @Test
    public void testGetSensorsEmpty() {
        assertTrue(route1.getSensors().isEmpty());
    }

    @Test
    public void testGetSensors() {
        List<Sensor> sensors = List.of(
                new DemoSpeedCamera(new int[] {60}, 60));
        try {
            route1.addSensor(sensors.get(0));
        } catch (DuplicateSensorException ignored) {}

        assertEquals(sensors, route1.getSensors());
    }

    @Test
    public void testGetSensorsNonModifiable() {
        try {
            route1.addSensor(new DemoSpeedCamera(new int[] {60}, 60));
            route1.addSensor(new DemoPressurePad(new int[] {40, 80, 60}, 50));
        } catch (DuplicateSensorException ignored) {}

        List<Sensor> sensors = route1.getSensors();

        sensors.add(new DemoSpeedCamera(new int[] {40}, 40));
        assertEquals(2, route1.getSensors().size());

        sensors.remove(1);
        assertEquals(2, route1.getSensors().size());
    }

    @Test
    public void testHasSpeedSignTrue() {
        route1.addSpeedSign(55);
        assertTrue(route1.hasSpeedSign());
    }

    @Test
    public void testHasSpeedSignFalse() {
        assertFalse(route1.hasSpeedSign());
    }
}
