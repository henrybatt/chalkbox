package tms.util;
import java.util.ArrayList;
import java.util.List;

public class TimedItemManager implements TimedItem{
    /**
     * A singleton TimedItemManager. Only one should exist in the whole program
     */
    private static TimedItemManager timedItemManager = null;
    private List<TimedItem> timedItems = new ArrayList<>();

    private TimedItemManager(){
        /**
         * In order to ensure correct singleton behaviour, the constructor is private
         */
    }
    public void registerTimedItem(TimedItem timedItem){
        /**
         * Registers a TimedItem such that it is called on oneSecond().
         * @param a TimedItem to register with the manager
         */
        timedItems.add(timedItem);
    }
    public static TimedItemManager getTimedItemManager(){
        /**
         * Gets a singleton instance of the TimedItemManager and makes one if required
         * @return the singleton instance of the TimedItemManager
         */
        if(timedItemManager == null) {
            timedItemManager = new TimedItemManager();
        }
        return timedItemManager;
    }
    public void oneSecond(){
        /**
         * Calls oneSecond() on each registered TimedItem.
         * @see oneSecond in interface TimedItem
         */
        for(TimedItem timedItem:timedItems){
            timedItem.oneSecond();
        }
    }
}