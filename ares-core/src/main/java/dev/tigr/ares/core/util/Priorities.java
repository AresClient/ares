package dev.tigr.ares.core.util;

public final class Priorities {
    public static final int MIXIN = Integer.MAX_VALUE;

    public static class Rotation {
        //All rotation priorities should be unique - so they can be used for keys as well
        public static final int
                //100 and below - Auras
                HOPPER_AURA = 95,
                KILL_AURA = 96, // When not YawStepping
                FIREWORK_AURA = 97,
                ANCHOR_AURA = 98,
                CRYSTAL_AURA = 99, // When not YawStepping
                BED_AURA = 100,

                //200 and below - Offensive Non-Aura Modules
                AUTO_TRAP = 198,
                HOLE_FILLER = 199,
                AUTO_32K = 200,

                //300 and below - Important to perform as soon as possible
                PACKET_MINE = 298,
                SCAFFOLD = 299,
                YAW_STEP = 300, // Not to be used as a key

                //400 and below - Defensive Modules
                ANTI_BED_AURA = 398,
                SURROUND = 399,
                BURROW = 400;
    }
}
