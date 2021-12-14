package dev.tigr.ares.core.util.math;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.util.math.doubles.V2D;

public class MathUtils implements Wrapper {
    public static V2D getMovement(final double speed, float forwards, float sideways, float yawDegrees) {
        return getMovement(speed, forwards, sideways, Math.toRadians(yawDegrees));
    }

    public static V2D getMovement(final double speed, float forwards, float sideways, double yaw) {
        if(forwards != 0) {
            if(sideways > 0) yaw += forwards > 0 ? - Math.PI / 4 : Math.PI / 4;
            else if(sideways < 0) yaw += forwards > 0 ? Math.PI / 4 : - Math.PI / 4;

            sideways = 0;

            if(forwards > 0) forwards = 1;
            else if(forwards < 0) forwards = -1;
        }

        yaw += Math.PI / 2;

        return new V2D(
                forwards * speed * Math.cos(yaw) + sideways * speed * Math.sin(yaw),
                forwards * speed * Math.sin(yaw) - sideways * speed * Math.cos(yaw)
        );
    }
}
