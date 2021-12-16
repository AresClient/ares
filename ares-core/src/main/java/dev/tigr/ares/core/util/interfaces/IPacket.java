package dev.tigr.ares.core.util.interfaces;

import dev.tigr.ares.core.util.math.floats.V2F;

public interface IPacket {
    void hotbarSlotUpdate(int slot);

    void playerRotation(V2F rotation);
}
