package org.aresclient.ares.api;

import org.aresclient.ares.api.math.Box;
import org.aresclient.ares.api.math.Vec3d;

public interface Entity {
    EntityType getEntityType();

    boolean isSameAs(Entity entity);

    Box getBoundingBox();

    double getPrevX();
    double getPrevY();
    double getPrevZ();

    Vec3d getPos();

    double getX();
    double getY();
    double getZ();
    double getLastRenderX();
    double getLastRenderY();
    double getLastRenderZ();

    boolean isOnGround();
    void setOnGround(boolean value);

    float getRenderHeadYaw();
    void setRenderHeadYaw(float headYaw);

    void setRenderBodyYaw(float bodyYaw);
}
