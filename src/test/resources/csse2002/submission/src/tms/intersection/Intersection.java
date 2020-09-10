package tms.intersection;

import tms.route.Route;
import tms.util.RouteNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Intersection {
    /**
     * Creates an Intersection Object
     */
    private String id;
    private List<Route> connections = new ArrayList<>();
    private List<Intersection> connectedIntersections = new ArrayList<>();


    public Intersection(String id){
        /**
         * Constructor for Intersection class
         * @param id String of the id of thi intersection
         */
        this.id = id;
    }
    public String getId(){
        /**
         * @return the String id of this intersection
         */
        return id;
    }
    public List<Route> getConnections() {
        /**
         * @return an ArrayList containing all the Routes
         * that connect to this intersection
         */
        return connections;
    }
    public List<Intersection> getConnectedIntersections() {
        /**
         * @return an ArrayList containing all the Intersections
         * that connect to this intersection
         */
        return connectedIntersections;
    }
    public void addConnection(Intersection from, int defaultSpeed) {
        /**
         * Creates a route between this intersection and Intersection from and adds it to connections
         * list also adds from Intersection to connectedIntersections list. Does nothing if
         * IllegalArgumentException or IllegalStateException thrown
         *
         * @param from an intersection from which a route is to be created
         * @param defaultSpeed the default speed that the route will take
         *                     must be positive
         * @throws IllegalArgumentException if default speed is negative
         * @throws IllegalStateException if route is already present
         */
        Route newRoute = new Route((from.getId()+":"+getId()),from, defaultSpeed);
        boolean duplicateRoute = false;
        // Check if route already present in connections
        for(Route route:connections){
            if (route.getFrom() == newRoute.getFrom()) {
                duplicateRoute = true;
                break;
            }
        }
        if (defaultSpeed<0){
            throw new IllegalArgumentException ("Default speed cannot be negative");
        }if(duplicateRoute){
            throw new IllegalStateException("This route is already in the intersection");
        }else {
            connections.add(newRoute);
            connectedIntersections.add(from);
        }
    }
    public void reduceIncomingSpeedSigns(){
        /**
         * Reduces the speed limit on incoming routes to this intersection.
         * All incoming routes with an electronic speed sign will have their speed limit
         * changed to be the greater of 50 and the current displayed speed minus 10.
         * Routes without an electronic speed sign should not be affected.
         */
        for (Route route : connections) {
            if(route.hasSpeedSign()) {
                route.setSpeedLimit(Math.max(50, (route.getSpeed() - 10)));
            }
        }
    }
    public Route getConnection(Intersection from) throws RouteNotFoundException {
        Route result;
        /**
         * @param from an intersection that is connected to this intersection
         * @return the route that goes from 'from' to this intersection
         * @throws RouteNotFoundException if no route exists from the given intersection to this intersection
         *
         * Given an origin intersection, returns the route that connects it to this destination intersection.
         */
        Route connection = null;
        for (Route route : connections) {
            if (route.getFrom() == from) {
                connection = route;
            }
        }
        if (connection == null) {
            throw new RouteNotFoundException("Route not found");
        } else {
            result = connection;
        }
        return result;
    }
    @Override
    public String toString(){
        /**
         * @return string represenation of the intersection
         */
        //For example, an intersection with the an ID of "ABC" and traffic
        // lights with a string representation of "3:X,Y,Z" would have a
        // toString() value of "ABC:3:X,Y,Z".
        return id;
    }
}
