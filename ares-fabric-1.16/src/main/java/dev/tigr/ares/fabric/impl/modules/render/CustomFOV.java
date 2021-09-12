package dev.tigr.ares.fabric.impl.modules.render;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.FloatSetting;

/***
 * @author Makrennel 09/11/21
 */
@Module.Info(name = "CustomFOV", description = "Allows you to set a custom field of view", category = Category.RENDER, alwaysListening = true)
public class CustomFOV extends Module {
    private final Setting<Double> verticalFOV = register(new DoubleSetting("Vertical FOV", 120, 1, 180));
    private final Setting<Double> horizontalFOV = register(new DoubleSetting("Horizontal FOV", 0, 1, 180));
    private final Setting<Float> fovEffectDegree = register(new FloatSetting("FOV Effect Degree", 0, 0, 2));

    // Hidden Setting for storing value of original FOV
    private final Setting<Double> originalFOV = register(new DoubleSetting("Original FOV", 90, 50, 120)).setVisibility(() -> verticalFOV.getValue() == 69420);
    private final Setting<Float> originalEffectDegree = register(new FloatSetting("Original FOV Degree", 0, 0, 1)).setVisibility(() -> fovEffectDegree.getValue() == 69420);

    private double lastVertFOV = verticalFOV.getValue();
    private double lastHorzFOV = horizontalFOV.getValue();

    @Override
    public void onEnable() {
        originalFOV.setValue(MC.options.fov);
        MC.options.fov = lastVertFOV;
        originalEffectDegree.setValue(MC.options.fovEffectScale);
        MC.options.fovEffectScale = fovEffectDegree.getValue();
    }

    // Restore original FOV setting
    @Override
    public void onDisable() {
        MC.options.fov = originalFOV.getValue();
        MC.options.fovEffectScale = originalEffectDegree.getValue();
    }

    @Override
    public void onRender3d() {
        // Only modify fov if the module is enabled and the fov doesn't match
        if(getEnabled()) {
            if(MC.options.fov != lastVertFOV) MC.options.fov = lastVertFOV;
            if(MC.options.fovEffectScale != fovEffectDegree.getValue()) MC.options.fovEffectScale = fovEffectDegree.getValue();
        }

        if(MC.currentScreen == null) return;

        // If the horizontal FOV setting has been modified, calculate the vertical fov equivalent and save new last FOV variables
        if(horizontalFOV.getValue() != lastHorzFOV) {
            verticalFOV.setValue(getVerticalFOV(horizontalFOV.getValue()));
            lastVertFOV = verticalFOV.getValue();
            lastHorzFOV = horizontalFOV.getValue();
            return;
        }

        // If the vertical FOV setting has been modified calculate the horizontal fov equivalent and save new last FOV variables.
        if(verticalFOV.getValue() != lastVertFOV) {
            horizontalFOV.setValue(getHorizontalFOV(verticalFOV.getValue()));
            lastVertFOV = verticalFOV.getValue();
            lastHorzFOV = horizontalFOV.getValue();
        }
    }

    // Calculate the vertical FOV from the given horizontal FOV and the size of the window
    public double getVerticalFOV(double horizontalFOV) {
        double a = Math.toDegrees(Math.tan(Math.toRadians(horizontalFOV /2))); // idk how radians work
        double b = (double)MC.currentScreen.height /(double)MC.currentScreen.width;
        double c = Math.toDegrees(Math.atan(Math.toRadians(a *b)));
        return c *2;
    }

    // Calculate the vertical FOV from the given vertical FOV and the size of the window
    public double getHorizontalFOV(double verticalFOV) {
        double a = Math.toDegrees(Math.tan(Math.toRadians(verticalFOV /2)));
        double b = (double)MC.currentScreen.width /(double)MC.currentScreen.height;
        double c = Math.toDegrees(Math.atan(Math.toRadians(a *b)));
        return c *2;
    }
}