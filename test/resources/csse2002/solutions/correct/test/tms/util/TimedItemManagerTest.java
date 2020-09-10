package tms.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TimedItemManagerTest {
    private DummyTimedItem[] items;

    @Before
    public void makeTimedItems() {
        items = new DummyTimedItem[4];
        for (int i = 0; i < 4; i++)  {
            items[i] = new DummyTimedItem();
        }
    }

    @Test
    public void oneSecondTest() {
        TimedItemManager.getTimedItemManager().oneSecond();
        TimedItemManager.getTimedItemManager().oneSecond();
        for (int i = 0; i < 4; i++) {
            assertEquals(2, items[i].getCounter());
        }
    }

    @Test
    public void singletonTest() {
        TimedItemManager manager1 = TimedItemManager.getTimedItemManager();
        TimedItemManager manager2 = TimedItemManager.getTimedItemManager();
        assertTrue("TimedItemManager is not a Singleton.", manager1 == manager2);
    }

    private class DummyTimedItem implements TimedItem {
        private int counter;

        DummyTimedItem() {
            counter = 0;
            TimedItemManager.getTimedItemManager().registerTimedItem(this);
        }

        @Override
        public void oneSecond() {
            counter++;
        }

        int getCounter(){
            return counter;
        }
    }
}
