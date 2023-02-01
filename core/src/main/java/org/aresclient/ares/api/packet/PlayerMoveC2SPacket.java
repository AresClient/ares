package org.aresclient.ares.api.packet;

import org.aresclient.ares.Ares;
import org.aresclient.ares.api.math.Vec2f;
import org.aresclient.ares.api.math.Vec3d;

public interface PlayerMoveC2SPacket {
    interface OnGround extends PlayerMoveC2SPacket {
        static OnGround create(boolean onGround) {
            return Ares.INSTANCE.creator.createPlayerMoveC2SPacketOnGround(onGround);
        }

        boolean isOnGround();
        void setOnGround(boolean value);
    }

    interface Position extends PlayerMoveC2SPacket.OnGround {
        static Position create(double x, double y, double z, boolean onGround) {
            return Ares.INSTANCE.creator.createPlayerMoveC2SPacketPosition(x, y, z, onGround);
        }

        static Position create(Vec3d position, boolean onGround) {
            return Ares.INSTANCE.creator.createPlayerMoveC2SPacketPosition(position.getX(), position.getY(), position.getZ(), onGround);
        }

        double getX();
        double getY();
        double getZ();
        void setX(double value);
        void setY(double value);
        void setZ(double value);

        default Vec3d getPos() {
            return Vec3d.create(getX(), getY(), getZ());
        }

        default void setPos(Vec3d pos) {
            setX(pos.getX());
            setY(pos.getY());
            setZ(pos.getZ());
        }

        default void setPos(double x, double y, double z) {
            setX(x);
            setY(y);
            setZ(z);
        }
    }

    interface Rotation extends PlayerMoveC2SPacket.OnGround {
        static Rotation create(float yaw, float pitch, boolean onGround) {
            return Ares.INSTANCE.creator.createPlayerMoveC2SPacketRotation(yaw, pitch, onGround);
        }

        static Rotation create(Vec2f rotation, boolean onGround) {
            return Ares.INSTANCE.creator.createPlayerMoveC2SPacketRotation(rotation.getX(), rotation.getY(), onGround);
        }

        float getYaw();
        float getPitch();
        void setYaw(float value);
        void setPitch(float value);

        default Vec2f getRotation() {
            return Vec2f.create(getYaw(), getPitch());
        }

        default void setRotation(Vec2f rotation) {
            setYaw(rotation.getX());
            setPitch(rotation.getY());
        }

        default void setRotation(float yaw, float pitch) {
            setYaw(yaw);
            setPitch(pitch);
        }
    }

    interface PositionRotation extends PlayerMoveC2SPacket.Rotation, PlayerMoveC2SPacket.Position {
        static PositionRotation create(double x, double y, double z, float yaw, float pitch, boolean onGround) {
            return Ares.INSTANCE.creator.createPlayerMoveC2SPacketPositionRotation(x, y, z, yaw, pitch, onGround);
        }

        static PositionRotation create(Vec3d position, Vec2f rotation, boolean onGround) {
            return Ares.INSTANCE.creator.createPlayerMoveC2SPacketPositionRotation(position.getX(), position.getY(), position.getZ(), rotation.getX(), rotation.getY(), onGround);
        }
    }
}
