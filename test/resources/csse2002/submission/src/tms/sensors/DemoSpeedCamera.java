package tms.sensors;

public class DemoSpeedCamera extends DemoSensor implements SpeedCamera{
    /**
     * Creates a DemoSpeedCamera object that extends DemoSensor and implements SpeedCamera
     */
    private int[] data;
    public DemoSpeedCamera(int[] data, int threshold) {
        /**
         * Constructor for DemoSpeedCamera
         * @param data non-empty array of data values
         * @param threshold value that indicates which values represent high congestion
         * @see DemoSensor
         */
        super(data, threshold);
        this.data = data;

    }
    public int averageSpeed(){
        /**
         * Returns the observed average speed of vehicles travelling past this sensor in km/h.
         * @return the current average speed in km/h reported by the speed camera
         */
        return super.getCurrentValue();
    }
    public int getCongestion(){
        /**
         * Calculates the congestion rate
         * @return congestion rate as an integer between 0 and 100 inclusive
         */
        return 100-(100*((int)((float)(averageSpeed())/(float)(getThreshold()))));
    }
    @Override
    public String toString(){
        /**
         * @return string representation of this sensor
         * "SC:threshold:list,of,data,values"
         */
        StringBuilder output = new StringBuilder("SC:" + getThreshold() + ":");
        for(int number: data){
            output.append(number).append(",");
        }
        return output.toString().replaceAll(",$", "");
    }
}