package dev.tigr.ares.core.feature.module;

import java.util.ArrayList;

/**
 * @author Tigermouthbear
 */
public enum Category {
    COMBAT,
    EXPLOIT,
    HUD,
    MOVEMENT,
    PLAYER,
    RENDER,
    MISC;

    private final ArrayList<Module> modules = new ArrayList<>();

    public Category add(Module module) {
        modules.add(module);
        return this;
    }

    public ArrayList<Module> getModules() {
        return modules;
    }
}
