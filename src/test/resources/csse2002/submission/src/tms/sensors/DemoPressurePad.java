package tms.sensors;

public class DemoPressurePad extends DemoSensor implements PressurePad {
    /**
     * Creates a new pressure pad sensor that extends DemoSensor and implements PressurePad
     */
    private int[] data;

    public DemoPressurePad(int[] data, int threshold) {
        /**
         * Constructor for DemoPressurePad
         * @param data a non-empty array of data values
         * @param threshold value that indicates which values represent high congestion
         */
        super(data, threshold);
        this.data=data;
    }

    public int countTraffic(){
        /**
         * Returns the number of vehicles currently waiting on the pressure pad.
         * @return the current traffic count reported by the pressure pad
         */
        return super.getCurrentValue();
    }
    @Override
    public int getCongestion(){
        /**
         * Calculates the congestion rate as the percentage given by countTraffic() divided by getThreshold().
         * Floating point division should be used when performing the calculation,
         * however the resulting floating point number should be rounded to the nearest integer before being returned.
         * @see getCongestion in interface Sensor
         * @return the calculated congestion rate as an integer between 0 and 100 inclusive
         */
        return (int)(100*(float)(countTraffic()/getThreshold()));
    }
    @Override
    public String toString(){
        /**
         * @return string representation of this sensor
         * "PP:threshold:list,of,data,values"
         */
        StringBuilder output = new StringBuilder("PP:" + getThreshold() + ":");
        for(int number: data){
            output.append(number).append(",");
        }
        return output.toString().replaceAll(",$", "");
    }
}