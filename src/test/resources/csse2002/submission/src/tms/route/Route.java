package tms.route;

import tms.intersection.Intersection;
import tms.sensors.Sensor;
import tms.util.DuplicateSensorException;
import java.util.ArrayList;
import java.util.List;

public class Route {
    private String id;
    private int defaultSpeed;
    private Intersection from;
    private List<Sensor> sensors = new ArrayList<>();
    private boolean hasSpeedSign;
    private SpeedSign speedSign;
    private TrafficLight trafficLight = null;


    public Route(String id, Intersection from, int defaultSpeed){
        /**
         * Constructor for the Route class
         * @param id takes in a string and assigns it as the ID of the route
         * @param from the intersection this route comes from. Goes to the
         *             intersection that generated this route
         * @param defaultSpeed the default speed of the route.
         */
        this.id = id;
        this.from = from;
        this.defaultSpeed = defaultSpeed;
    }
    public Intersection getFrom(){
        /**
         * @return the intersection this route comes from
         */
        return from;
    }
    public TrafficLight getTrafficLight(){
        /**
         * @return the TrafficLight instance deployed on the route or null of no TrafficLight
         */
        return trafficLight;
    }
    public List<Sensor> getSensors(){
        /**
         * Returns a new list containing all the sensors on this route.
         * @return list of all sensors on this route
         */
        return sensors;
    }
    public boolean hasSpeedSign(){
        /**
         * @return true if there is an electronic speed sign , else returns false
         */
        return hasSpeedSign;
    }
    public int getSpeed(){
        /**
         * Returns the currently active speed limit for vehicles on this route.
         * If an electronic speed sign is present, return its displayed speed.
         * Otherwise, return the default speed limit of the route.
         * @return speed limit of the route
         */
        if(hasSpeedSign()){ // Check if there is a speed sign using hasSpeedSign flag
            return speedSign.getCurrentSpeed(); //if there is a speed sign, returns the speed on the sign
        }else{
            return defaultSpeed; // else returns the default speed of the route
        }
    }
    public void setSignal(TrafficSignal signal){
        /**
         * Sets the traffic signal if there is a traffic light controlling traffic flow on this route.
         * If there is no traffic light for this route, no action should be taken.
         * @param signal the traffic light signal for the route
         */
        if (trafficLight!= null) {
            trafficLight.setSignal(signal);
        }
    }
    public void addTrafficLight(){
        /**
         * Adds a TrafficLight signal to the route. Default signal is RED
         * Checks if there is not a traffic light, if none, adds a traffic light
         * otherwise does nothing
         */
        if(trafficLight == null){
            trafficLight = new TrafficLight();
        }
    }
    public void addSpeedSign(int initialSpeed){
        /**
         * Creates and adds a new electronic speed sign to this route.
         * Also Overwrites any current speed sign
         * @param initialSpeed the initial speed of the speed sign
         * @throws IllegalArgumentException if int initialSpeed is negative
         */
        if (initialSpeed<0){
            throw new IllegalArgumentException("Given speed is negative");
        }
        hasSpeedSign = true;
        speedSign = new SpeedSign(initialSpeed);

    }
    public void setSpeedLimit(int newSpeed){
        /**
         * Sets the speed limit of this route to the given value.
         * Changes the speed on speed sign only, not the default speed on the route
         * @param newSpeed the new speed limit to be displayed
         * @throws IllegalStateException if the route has no electronic speed sign
         * @throws IllegalArgumentException if the given speed is negative
         */
        if(hasSpeedSign) { //check that there is a speed sign
            if(newSpeed<0){
                throw new IllegalArgumentException("Given speed is negative");
            }else{
            speedSign.setCurrentSpeed(newSpeed);
            }
        }else{
            throw new IllegalStateException("Route has no electronic speed sign");
        }
    }
    public void addSensor(Sensor sensor) throws DuplicateSensorException {
        /**
         * Adds a sensor to the route if a sensor of the same type is not already on the route.
         * @param sensor the sensor to add to the route
         * @throws DuplicateSensorException if adding same type of sensor on the route
         */
        char p = 'P';

        if (sensors.contains(sensor)) {
            throw new DuplicateSensorException("This sensor (" + sensor + ") is already here");
        } else {
            if (sensors.size() == 1) { // Sorting between PressurePad sensor and SpeedCamera sensor
                if (sensor.toString().charAt(0) == p) {
                    sensors.add(0, sensor);
                }
            }else {
                sensors.add(sensor);
            }
        }
    }
    public String toString(){
        /**
         * Returns the string representation of this route.
         * The format of the string "id:defaultSpeed:numberOfSensors"
         * If this route has a SpeedSign, then the format  "id:defaultSpeed:numberOfSensors:speedSignSpeed"
         * If this route has sensors, returns an additional line for information pertaining to each sensor on the route. The order in which these lines appear should be alphabetical, meaning a line for a pressure plate (PP) should come before a line for a speed camera (SC).
         *
         * Each sensor line should contain that sensor's string representation
         */
        String ls = System.lineSeparator();
        StringBuilder output = new StringBuilder(id + ":" + defaultSpeed + ":" + sensors.size());
        if(hasSpeedSign){
            output.append(":").append(speedSign.getCurrentSpeed());
        }
        if(sensors.size()>0){
            for(Sensor sensor:getSensors()){
                output.append(ls).append(sensor);
            }
        }
        return output.toString();
    }

}
