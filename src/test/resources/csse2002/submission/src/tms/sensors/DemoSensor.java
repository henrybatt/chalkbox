package tms.sensors;
import tms.util.TimedItem;

public abstract class DemoSensor extends Object implements TimedItem {
    /**
     * An implementation of the sensor interface
     */
    private int[] data;
    private int threshold;
    private int currentIndex = 0;

    protected DemoSensor(int[] data, int threshold){
        /**
         * Constructor for the DemoSensor
         * @param data a non-empty array of data values
         * @param threshold a threshold value that indicated what value is high congestion
         */
        this.data = data;
        this.threshold = threshold;
    }
    protected int getCurrentValue(){
        /**
         * Returns the current data value as measured by the sensor.
         * @return current data value
         */
        return data[currentIndex];
    }
    public int getThreshold(){
        /**
         * Returns the threshold data value
         * @return threshold value
         */
        return threshold;
    }
    public void oneSecond(){
        /**
         * Sets the current data value returned by getCurrentValue()
         * to be the next value in the data array passed to the constructor.
         * @see oneSecond in interface TimedItem
         */
        System.out.println("One second");
        if (currentIndex==data.length-1){ // If current index is on the last index, wrap around
            currentIndex=0;
        }else{
            currentIndex++;
        }
    }
    public String toString(){
        /**
         * @return string representation:
         * "threshold:list,of,data,values" where 'threshold' is this sensor's threshold and
         * 'list,of,data,values' is this sensor's data array
         */
        StringBuilder output = new StringBuilder("" + threshold + ":");
        for(int num: data){
            output.append(num);
        }
        return output.toString().replaceAll(",$", "");
    }
}

