package tms.sensors;

public interface PressurePad extends Sensor{
    int countTraffic(); // Returns the number of vehicles currently waiting on the pressure pad.
}
