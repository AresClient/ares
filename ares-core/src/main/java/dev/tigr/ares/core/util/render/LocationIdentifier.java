package dev.tigr.ares.core.util.render;

/**
 * @author Tigermouthbear
 */
public class LocationIdentifier {
    private final String path;

    public LocationIdentifier(String path) {
        this.path = "/assets/ares/" + path;
    }

    public String getPath() {
        return path;
    }
}