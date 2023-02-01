package org.aresclient.ares.api.math;

import org.aresclient.ares.Ares;

public interface Facing {
    enum EnumValue {
        DOWN,
        UP,
        NORTH,
        SOUTH,
        WEST,
        EAST;

        public static final EnumValue[] VALUES = values();

        Facing asFacing() {
            return Ares.INSTANCE.creator.facing(ordinal());
        }
    }
    default EnumValue asEnumValue() {
        return EnumValue.VALUES[getID()];
    }

    int getID();

    Vec3i getOffset();
}
