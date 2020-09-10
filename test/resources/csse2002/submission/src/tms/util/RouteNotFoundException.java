package tms.util;

public class RouteNotFoundException extends Throwable {
    /**
     * Exception thrown when a sensor is added to a route that already contains a sensor of the same type.
     * @param message what to print to console
     */
    public RouteNotFoundException(String message){
        System.out.println(message);
    }
}
