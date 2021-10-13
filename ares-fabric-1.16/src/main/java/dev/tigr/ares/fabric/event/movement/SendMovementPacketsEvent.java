package dev.tigr.ares.fabric.event.movement;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class SendMovementPacketsEvent extends Event {
    private Vec3d pos;
    protected Vec2f rotation;
    private boolean onGround;
    private boolean modifying = false;

    public Vec3d getPos()
    {
        return pos;
    }

    public Vec2f getRotation()
    {
        return rotation;
    }

    public float getYaw()
    {
        return rotation.x;
    }

    public float getPitch()
    {
        return rotation.y;
    }

    public boolean isOnGround()
    {
        return onGround;
    }

    public void setPos(Vec3d pos)
    {
        this.pos = pos;
    }

    public void setRotation(Vec2f rotation) {
        this.modifying = true;
        this.rotation = rotation;
    }

    public void setRotation(float[] rotation)
    {
        this.modifying = true;
        this.rotation = new Vec2f(rotation[0], rotation[1]);
    }

    public void setYaw(float yaw)
    {
        this.modifying = true;
        this.rotation = new Vec2f(yaw, rotation.y);
    }

    public void setPitch(float pitch)
    {
        this.modifying = true;
        this.rotation = new Vec2f(rotation.x, pitch);
    }

    public void setOnGround(boolean onGround)
    {
        this.onGround = onGround;
    }

    public boolean isModifying() {
        return modifying;
    }

    public static class Pre extends SendMovementPacketsEvent {
        public Pre(Vec3d pos, boolean onGround) {
            setPos(pos);
            setOnGround(onGround);
        }
    }

    public static class Post extends SendMovementPacketsEvent {
        public Post(Vec2f rotation) {
            this.rotation = rotation;
        }

        public Post(float yaw, float pitch) {
            this.rotation = new Vec2f(yaw, pitch);
        }

        public Post(float[] rotation) {
            this.rotation = new Vec2f(rotation[0], rotation[1]);
        }
    }
}
