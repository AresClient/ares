package org.aresclient.ares.api.instrument.global;

import org.aresclient.ares.api.Ares;
import org.aresclient.ares.api.instrument.Instrument;
import org.aresclient.ares.api.setting.Setting;

/**
 * Globals are used for grouping settings and functions which interact with multiple
 * modules but do not themselves directly do anything, and also holds related utility
 * functions to the purpose of the Global
 */
public class Global extends Instrument {
    private static final Setting.Map<?> SETTINGS = Ares.getSettings().addMap("Globals");

    public Global(String name, String description) {
        super(name, description, SETTINGS);
    }
}