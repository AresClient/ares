package org.aresclient.ares.api.instrument;

import org.aresclient.ares.api.Ares;
import org.aresclient.ares.api.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public abstract class Instrument {
    private final String name;
    private final String description;

    protected final Setting.Map<?> settings;
    protected final List<Component<?>> components = new ArrayList<>();

    public Instrument(String name, String description, Setting.Map<?> parentSettings) {
        this.name = name;
        this.description = description;
        this.settings = parentSettings.addMap(name);
    }

    public void tick() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    void addComponent(Component<?> component) {
        components.add(component);
        if(component instanceof Component.Settings<?> settingsComponent)
            settingsComponent.setSettings(this.settings.addMap(settingsComponent.getPathName()));
    }

    public void registerComponents() {
        for(Component<?> component : components) {
            if(component instanceof Component.Listener<?,?>) {
                Ares.getEventManager().register(component);
                Ares.getEventManager().register(component.getClass());
            }
        }
    }

    public void unregisterComponents() {
        for(Component<?> component : components) {
            if(component instanceof Component.Listener<?,?>) {
                Ares.getEventManager().unregister(component);
                Ares.getEventManager().unregister(component.getClass());
            }
        }
    }
}
