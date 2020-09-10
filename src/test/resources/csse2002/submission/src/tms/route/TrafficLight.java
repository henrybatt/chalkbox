package tms.route;

public class TrafficLight {
    /**
     * Creates a TrafficLight object to be added to the route
     */
    private TrafficSignal signal;
    public TrafficLight(){
        /**
         * Constructor for TrafficLight object
         * Makes the default TrafficSignal RED
         */
        signal = TrafficSignal.RED;
    }
    public TrafficSignal getSignal() {
        /**
         * Returns the signal of the TrafficLight
         * @return the colour of the TrafficLight
         */
        return signal;
        }
    public void setSignal(TrafficSignal signal) {
        /**
         * Sets the signal on the TrafficLight using TrafficSignal ENUM
         * @param signal the new signal of the TrafficLight
         */
        this.signal = signal;
        }
}

