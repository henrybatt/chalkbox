package tms.util;

public class DuplicateSensorException extends Throwable {
    /**
     * Exception thrown when a sensor is added to a route that already contains a sensor of the same type.
     * @param message what to print to console
     */
    public DuplicateSensorException(String message){
        System.out.println(message);
    }
}
