package dev.tigr.ares.forge.event.events.render;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

public class RenderHeldItemEvent extends Event {
    private RenderHeldItemEvent() {
    }

    public static class Invoke extends RenderHeldItemEvent {
    }

    public static class Cancelled extends RenderHeldItemEvent {
        EnumHand hand;
        Vec3d translate = new Vec3d(0,0,0);
        Vector3f scale = new Vector3f(1,1,1);
        Quaternion rotation = new Quaternion();

        public void translate(double x, double y, double z) {
            translate = translate.add(x, y, z);
        }

        public void scale(float x, float y, float z) {
            scale.translate(x, y, z);
        }

        public void multiply(Quaternion quaternion) {
            rotation = quaternion;
        }

        public Cancelled(EnumHand hand) {
            this.hand = hand;
        }

        public EnumHand getHand() {
            return hand;
        }

        public Vec3d getTranslation() {
            return translate;
        }

        public Vector3f getScale() {
            return scale;
        }

        public Quaternion getRotation() {
            return rotation;
        }
    }
}
