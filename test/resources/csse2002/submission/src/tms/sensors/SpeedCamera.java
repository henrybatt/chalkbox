package tms.sensors;

public interface SpeedCamera extends Sensor{
    int averageSpeed(); // Returns the observed average speed of vehicles travelling past this sensor
}
