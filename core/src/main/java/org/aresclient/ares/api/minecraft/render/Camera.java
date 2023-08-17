package org.aresclient.ares.api.minecraft.render;

import org.aresclient.ares.api.minecraft.math.Vec3d;

public interface Camera {
    Vec3d getPos();

    double getX();
    double getY();
    double getZ();

    float getPitch(); // playerViewX
    float getYaw(); // playerViewY
}
