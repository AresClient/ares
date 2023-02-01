package org.aresclient.ares.api;

import org.aresclient.ares.api.math.Vec3d;

public interface Camera {
    Vec3d getPos();

    double getX();
    double getY();
    double getZ();

    float getPitch(); // playerViewX
    float getYaw(); // playerViewY
}
