package tms.sensors;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DemoPressurePadTest {
    private DemoPressurePad testSensor;
    private int[] data;
    private int threshold;

    @Before
    public void setup(){
        data = new int[] {0, 1, 2, 3, 4, 5, 6};
        threshold = 4;
        testSensor = new DemoPressurePad(data, threshold);
    }

    @Test
    public void countTrafficNoTimeTest() {
        assertEquals(testSensor.countTraffic(), data[0]);
    }

    @Test
    public void countTrafficSomeTimeTest() {
        testSensor.oneSecond();
        testSensor.oneSecond();
        testSensor.oneSecond();
        assertEquals(testSensor.countTraffic(), data[3]);
    }

    @Test
    public void countTrafficArrayLoopTest() {
        for (int i = 0; i < data.length + 4; i++) {
            testSensor.oneSecond();
        }
        assertEquals(data[4], testSensor.countTraffic());
    }

    @Test
    public void getThreshold() {
        assertEquals(threshold, testSensor.getThreshold());
    }

    @Test
    public void getCongestionInitialTest() {
        assertEquals(0, testSensor.getCongestion());
    }

    @Test
    public void getCongestionBasicTest() {
        assertEquals(0, testSensor.getCongestion());

        testSensor.oneSecond();
        assertEquals(25, testSensor.getCongestion());

        testSensor.oneSecond();
        testSensor.oneSecond();
        testSensor.oneSecond();
        assertEquals(100, testSensor.getCongestion());
    }

    @Test
    public void getCongestionOutOfBoundsTest() {
        testSensor.oneSecond();
        testSensor.oneSecond();
        testSensor.oneSecond();
        testSensor.oneSecond();
        testSensor.oneSecond();
        testSensor.oneSecond();
        assertEquals(100, testSensor.getCongestion()); // over 100
    }

    @Test
    public void getCongestionRoundingTest() {
        DemoPressurePad testSensor2 = new DemoPressurePad(
                new int[] {50, 10}, 90);
        assertEquals(56, testSensor2.getCongestion()); // 50/90 = 0.55555
        testSensor2.oneSecond();
        assertEquals(11, testSensor2.getCongestion()); // 10/90 = 0.11111
    }

    @Test
    @Deprecated
    public void toStringTest() {
        assertEquals("PP:4:0,1,2,3,4,5,6", testSensor.toString());
    }
}
