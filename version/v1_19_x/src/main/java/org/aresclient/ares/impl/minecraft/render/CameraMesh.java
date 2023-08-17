package org.aresclient.ares.impl.minecraft.render;

import org.aresclient.ares.api.minecraft.AbstractMesh;
import org.aresclient.ares.api.minecraft.math.Vec3d;
import org.aresclient.ares.api.minecraft.render.Camera;

public class CameraMesh extends AbstractMesh<net.minecraft.client.render.Camera> implements Camera {
    public CameraMesh(net.minecraft.client.render.Camera value) {
        super(value);
    }

    @Override
    public Vec3d getPos() {
        return (Vec3d) getMeshValue().getPos();
    }

    @Override
    public double getX() {
        return getMeshValue().getPos().x;
    }

    @Override
    public double getY() {
        return getMeshValue().getPos().y;
    }

    @Override
    public double getZ() {
        return getMeshValue().getPos().z;
    }

    @Override
    public float getPitch() {
        return getMeshValue().getPitch();
    }

    @Override
    public float getYaw() {
        return getMeshValue().getYaw();
    }
}
