package pattern;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to hold a list of paths representing the common paths in a pattern
 * Is used to read the JSON fails containing the paths
 */
public class PathCollection {
    List<String> paths;
    String name;

    public PathCollection(){
        this.paths = new ArrayList<>();
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
