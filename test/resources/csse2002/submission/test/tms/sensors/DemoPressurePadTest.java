package tms.sensors;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class DemoPressurePadTest {
    @Test
    public void testGetCurrentValue() {
        int[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int threshold = 10;
        DemoPressurePad demoPressurePad = new DemoPressurePad(data, threshold);
    }

    @Test
    public void testGetThreshold() {
        int[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int threshold = 10;
        DemoPressurePad demoPressurePad = new DemoPressurePad(data, threshold);
        Assert.assertEquals(threshold, demoPressurePad.getThreshold());
    }

    @Test
    public void testCountTraffic() {
        int[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] data2 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int threshold = 10;
        DemoPressurePad demoPressurePad = new DemoPressurePad(data, threshold);
        Assert.assertEquals(data[0],demoPressurePad.countTraffic());
    }
    @Test
    public void testCountTrafficOneSecond() {
        int[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int threshold = 10;
        DemoPressurePad demoPressurePad = new DemoPressurePad(data, threshold);
        demoPressurePad.oneSecond();
        Assert.assertEquals(data[1],demoPressurePad.countTraffic());
    }
    @Test
    public void testCountTrafficOneSecondEndOfArray() {
        int[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int threshold = 10;
        DemoPressurePad demoPressurePad = new DemoPressurePad(data, threshold);
        for(int num: data) {
            demoPressurePad.oneSecond();
        }
        Assert.assertEquals(data[0],demoPressurePad.countTraffic());
    }

    @Test
    public void testGetCongestion() {
        int[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int threshold = 10;
        DemoPressurePad demoPressurePad = new DemoPressurePad(data, threshold);
        Assert.assertEquals((int)(100*(float)(demoPressurePad.countTraffic()/demoPressurePad.getThreshold())),
                demoPressurePad.getCongestion());
    }


    @Test
    public void testToString() {
        int[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int threshold = 10;
        DemoPressurePad demoPressurePad = new DemoPressurePad(data, threshold);

        StringBuilder output = new StringBuilder("PP:" + threshold + ":");
        for(int number: data){
            output.append(number).append(",");
        }
        String fin = output.toString().replaceAll(",$", "");
        Assert.assertEquals(fin,demoPressurePad.toString());
    }
}
