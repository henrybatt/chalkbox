package tms.sensors;
public interface Sensor{
    int getCongestion(); // Returns the level of congestion as detected by this sensor.
    int getThreshold(); // Returns the level below/above which observed data indicates congestion is occurring on a route
}