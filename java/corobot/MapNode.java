package corobot;
import java.util.List;

/**
 * Simple to class to represent a node in the map.
 * contains a name, location and neighbors (all public)
 */
public class MapNode {
    String name;
    public double x, y; // in meters
    List<String> nbrs;
}
