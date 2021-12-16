package dev.tigr.ares.core.event.movement;

import dev.tigr.ares.core.util.math.doubles.V3D;
import dev.tigr.ares.core.util.math.floats.V2F;
import dev.tigr.simpleevents.event.Event;

public class SendMovementPacketsEvent extends Event {
    private V3D pos;
    protected V2F rotation;
    private boolean onGround;
    private boolean modifying = false;

    public V3D getPos()
    {
        return pos;
    }

    public V2F getRotation()
    {
        return rotation;
    }

    public float getYaw()
    {
        return rotation.a;
    }

    public float getPitch()
    {
        return rotation.b;
    }

    public boolean isOnGround()
    {
        return onGround;
    }

    public void setPos(V3D pos)
    {
        this.pos = pos;
    }

    public void setRotation(V2F rotation) {
        this.modifying = true;
        this.rotation = rotation;
    }

    public void setRotation(float[] rotation)
    {
        this.modifying = true;
        this.rotation = new V2F(rotation[0], rotation[1]);
    }

    public void setYaw(float yaw)
    {
        this.modifying = true;
        this.rotation = new V2F(yaw, rotation.b);
    }

    public void setPitch(float pitch)
    {
        this.modifying = true;
        this.rotation = new V2F(rotation.a, pitch);
    }

    public void setOnGround(boolean onGround)
    {
        this.onGround = onGround;
    }

    public boolean isModifying() {
        return modifying;
    }

    public static class Pre extends SendMovementPacketsEvent {
        public Pre(V3D pos, boolean onGround) {
            setPos(pos);
            setOnGround(onGround);
        }
    }

    public static class Post extends SendMovementPacketsEvent {
        public Post(V2F rotation) {
            this.rotation = rotation;
        }

        public Post(float yaw, float pitch) {
            this.rotation = new V2F(yaw, pitch);
        }

        public Post(float[] rotation) {
            this.rotation = new V2F(rotation[0], rotation[1]);
        }
    }
}
