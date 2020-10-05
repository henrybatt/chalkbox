package tms.sensors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DemoSpeedCameraTest {
    private DemoSpeedCamera testSensor;
    private int[] data;
    private int threshold;

    @Before
    public void setup(){
        data = new int[] {60, 70, 60, 30, 15, 0};
        threshold = 60;
        testSensor = new DemoSpeedCamera(data, threshold);
    }

    @Test
    public void countTrafficNoTimeTest() {
        Assert.assertEquals(testSensor.averageSpeed(), data[0]);
    }

    @Test
    public void countTrafficSomeTimeTest() {
        testSensor.oneSecond();
        testSensor.oneSecond();
        testSensor.oneSecond();
        Assert.assertEquals(testSensor.averageSpeed(), data[3]);
    }

    @Test
    public void countTrafficArrayLoopTest() {
        for (int i = 0; i < data.length + 2; i++) {
            testSensor.oneSecond();
        }
        Assert.assertEquals(data[2], testSensor.averageSpeed());
    }

    @Test
    public void getThreshold() {
        Assert.assertEquals(threshold, testSensor.getThreshold());
    }

    @Test
    public void getCongestionInitialTest() {
        assertEquals(0, testSensor.getCongestion());
    }

    @Test
    public void getCongestionBasicTest() {
        testSensor.oneSecond();
        testSensor.oneSecond();
        assertEquals(0, testSensor.getCongestion());
        testSensor.oneSecond();
        Assert.assertEquals(50, testSensor.getCongestion());
        testSensor.oneSecond();
        Assert.assertEquals(75, testSensor.getCongestion());
        testSensor.oneSecond();
        Assert.assertEquals(100, testSensor.getCongestion());
    }

    @Test
    public void getCongestionOutOfBoundsTest() {
        testSensor.oneSecond();
        Assert.assertEquals(0, testSensor.getCongestion()); // under 0
    }

    @Test
    public void getCongestionRoundingTest() {
        DemoSpeedCamera testSensor2 = new DemoSpeedCamera(
                new int[] {50, 10}, 90);
        assertEquals(44, testSensor2.getCongestion()); // 1 - 50/90 = 0.44444
        testSensor2.oneSecond();
        assertEquals(89, testSensor2.getCongestion()); // 1 - 10/90 = 0.88888
    }

    @Test
    @Deprecated
    public void toStringTest() {
        assertEquals("SC:60:60,70,60,30,15,0", testSensor.toString());
    }
}
