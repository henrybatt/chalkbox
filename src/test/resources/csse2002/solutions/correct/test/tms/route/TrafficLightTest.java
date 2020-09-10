package tms.route;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TrafficLightTest {
    private TrafficLight testLight;

    @Before
    public void setup() {
        testLight = new TrafficLight();
    }

    @Test
    public void startsRedTest() {
        Assert.assertEquals(TrafficSignal.RED, testLight.getSignal());
    }

    @Test
    public void changeSignalTest() {
        testLight.setSignal(TrafficSignal.GREEN);
        Assert.assertEquals(TrafficSignal.GREEN, testLight.getSignal());
        testLight.setSignal(TrafficSignal.YELLOW);
        Assert.assertEquals(TrafficSignal.YELLOW, testLight.getSignal());
        testLight.setSignal(TrafficSignal.ERROR);
        Assert.assertEquals(TrafficSignal.ERROR, testLight.getSignal());
    }
}
