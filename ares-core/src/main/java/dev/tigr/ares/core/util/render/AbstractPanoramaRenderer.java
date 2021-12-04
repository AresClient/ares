package dev.tigr.ares.core.util.render;

/**
 * @author Tigermouthbear 11/28/21
 * provides render abstraction for drawing panorama backgrounds
 */
public abstract class AbstractPanoramaRenderer {
    public static final LocationIdentifier[] DEFAULT_PANORAMA_PATHS = new LocationIdentifier[]{
            new LocationIdentifier("textures/panorama/panorama_0.png"),
            new LocationIdentifier("textures/panorama/panorama_1.png"),
            new LocationIdentifier("textures/panorama/panorama_2.png"),
            new LocationIdentifier("textures/panorama/panorama_3.png"),
            new LocationIdentifier("textures/panorama/panorama_4.png"),
            new LocationIdentifier("textures/panorama/panorama_5.png")
    };

    protected final LocationIdentifier[] faces;

    public AbstractPanoramaRenderer(LocationIdentifier[] faces) {
        this.faces = faces;
    }

    public abstract void draw();
}
