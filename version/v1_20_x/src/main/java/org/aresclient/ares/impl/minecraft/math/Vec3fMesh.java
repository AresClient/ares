package org.aresclient.ares.impl.minecraft.math;

import org.aresclient.ares.api.minecraft.AbstractMesh;
import org.aresclient.ares.api.minecraft.math.Vec3f;
import org.joml.Vector3f;

public class Vec3fMesh extends AbstractMesh<Vector3f> implements Vec3f {
    public Vec3fMesh(Vector3f value) {
        super(value);
    }

    @Override
    public float getX() {
        return getMeshValue().x;
    }

    @Override
    public float getY() {
        return getMeshValue().y;
    }

    @Override
    public float getZ() {
        return getMeshValue().z;
    }

    @Override
    public void setX(float value) {
        getMeshValue().x = value;
    }

    @Override
    public void setY(float value) {
        getMeshValue().y = value;
    }

    @Override
    public void setZ(float value) {
        getMeshValue().z = value;
    }
}
