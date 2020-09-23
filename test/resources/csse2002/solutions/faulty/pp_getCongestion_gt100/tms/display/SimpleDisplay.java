package tms.display;

import tms.intersection.Intersection;
import tms.route.Route;
import tms.sensors.DemoSpeedCamera;
import tms.util.DuplicateSensorException;
import tms.util.RouteNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to create and display a version of your network.
 * <p>
 * This can be used to assist in the debugging and visualisation of your network
 * and will <strong>not</strong> be assessed.
 * <p>
 *  It is <em>highly recommended</em> you use this sparingly for visualisation purposes
 *  and use JUnit tests for the majority of your testing.
 * @given
 */
public class SimpleDisplay {

    /**
     * Creates a network of intersections that have routes between each other.
     * @return list of intersections storing the demo network that is created
     * @given
     */
    private static List<Intersection> instantiateClasses() {
        // TODO add your code to create your network for debugging purposes.
        // Sample below:
        List<Intersection> testNetwork = new ArrayList<>();

/* This code to create your network will not compile until you implement at
   least Intersection and Route. You will have to selectively uncomment parts
   of this code if you have only implemented part of the assignment. You may
   add more complexity to your network if you wish.

   Consequently, because at the moment nothing is added to testNetwork,
   nothing will be displayed if you attempt to run main in this class,
   until you uncomment some of the code below.

        // Add intersections to the test network.
        testNetwork.add(new Intersection("0"));
        testNetwork.add(new Intersection("1"));
        testNetwork.add(new Intersection("2"));

        // Connect intersections together to create routes between intersections.
        testNetwork.get(0).addConnection(testNetwork.get(2), 80);
        testNetwork.get(0).addConnection(testNetwork.get(1), 80);
        testNetwork.get(1).addConnection(testNetwork.get(2), 100);
        testNetwork.get(1).addConnection(testNetwork.get(0), 90);

        // Add sensors and signals to routes in the test network.
        int[] speedCameraData = {89, 87, 22, 32, 88};
        try {
            testNetwork.get(1).getConnection(testNetwork.get(0)).addSensor(
                    new DemoSpeedCamera(speedCameraData, 90));
            testNetwork.get(0).getConnection(testNetwork.get(2)).addSpeedSign(75);
        } catch (DuplicateSensorException | RouteNotFoundException e) {
            e.printStackTrace();
        }
*/

        return testNetwork;
    }

    /**
     * Displays the items in testNetwork using their toString methods.
     * <p>
     *     The display is in the format:
     *     <ul>
     *     <li>intersection: [INTERSECTION_NAME]
     *     </li><li>[Route1 toString()]
     *     </li><li>...
     *     </li><li>[Routen toString()]
     *     </li>
     *     </ul>
     *     for each intersection
     *
     * @param testNetwork a list of intersections that are to be displayed
     * @given
     */
    private static void display(List<Intersection> testNetwork) {
        StringBuilder intersectionDisplay = new StringBuilder();

        for (Intersection intersection : testNetwork) {
            intersectionDisplay.append("intersection: ")
                    .append(intersection.toString()).append(System.lineSeparator());

            for (Intersection incoming: intersection.getConnectedIntersections()) {
                try {
                    Route in = intersection.getConnection(incoming);
                    intersectionDisplay.append(in.toString()).append(System.lineSeparator());
                } catch (RouteNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(intersectionDisplay.toString());
    }

    /**
     * Uses supplied code in instantiateClasses() to create a network and then
     * uses display() to print that network.
     * @param args ignored
     * @given
     */
    public static void main(String[] args) {
        SimpleDisplay.display(instantiateClasses());
    }
}
