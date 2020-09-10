package tms.route;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SpeedSignTest {
    private SpeedSign speedSign;

    @Before
    public void setup() {
        speedSign = new SpeedSign(50);
    }

    @Test
    public void testConstructor() {
        assertEquals(50, speedSign.getCurrentSpeed());

        speedSign = new SpeedSign(80);
        assertEquals(80, speedSign.getCurrentSpeed());
    }

    @Test
    public void testSetCurrentSpeed() {
        speedSign.setCurrentSpeed(90);
        assertEquals(90, speedSign.getCurrentSpeed());
    }
}
