package org.aresclient.ares.api.module;

import org.aresclient.ares.api.Ares;
import org.aresclient.ares.api.render.MatrixStack;
import org.aresclient.ares.api.render.Renderer;
import org.aresclient.ares.api.setting.Setting;
import org.aresclient.ares.api.util.Keys;

public class Module {
    public enum TogglesOn { PRESS, RELEASE, HOLD }

    public static class Defaults {
        private boolean enabled = false;
        private int bind = Keys.UNKNOWN;
        private boolean visible = true;
        private TogglesOn togglesOn = TogglesOn.PRESS;
        private boolean alwaysListening = false;

        public Defaults setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Defaults setBind(int bind) {
            this.bind = bind;
            return this;
        }

        public Defaults setVisible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public Defaults setTogglesOn(TogglesOn togglesOn) {
            this.togglesOn = togglesOn;
            return this;
        }

        public Defaults setAlwaysListening(boolean alwaysListening) {
            this.alwaysListening = alwaysListening;
            return this;
        }
    }

    private final Category category;
    private final String name;
    private final String description;
    private final Defaults defaults;

    protected final Setting.Map<?> settings;
    private final Setting.Boolean enabled;
    private final Setting.Bind bind;
    private final Setting.Boolean visible;
    private final Setting.Enum<TogglesOn> togglesOn;

    public Module(Category category, String name, String description) {
        this(category, name, description, new Defaults());
    }

    public Module(Category category, String name, String description, Defaults defaults) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.defaults = defaults;

        settings = category.getSettings().addMap(name);
        enabled = settings.addBoolean("Enabled", defaults.enabled).setListener(value -> {
            if(value) {
                if(!defaults.alwaysListening) {
                    Ares.getEventManager().register(this);
                    Ares.getEventManager().register(getClass());
                }
                onEnable();
            } else {
                if(!defaults.alwaysListening) {
                    Ares.getEventManager().unregister(this);
                    Ares.getEventManager().unregister(getClass());
                }
                onDisable();
            }
        });
        bind = settings.addBind("Bind", defaults.bind).setCallback(state -> {
            TogglesOn toggles = getTogglesOn();
            if(toggles == TogglesOn.PRESS && state) toggle();
            else if(toggles == TogglesOn.RELEASE && !state) toggle();
            else if(toggles == TogglesOn.HOLD) setEnabled(state);
        });
        visible = settings.addBoolean("Visible", defaults.visible);
        togglesOn = settings.addEnum("Toggles On", defaults.togglesOn);
    }

    public void tick() {
        if(isListening()) onTick();
    }

    public void renderHud(float delta, Renderer.Buffers buffers, MatrixStack matrixStack) {
        if(isListening()) onRenderHud(delta, buffers, matrixStack);
    }

    public void renderWorld(float delta, Renderer.Buffers buffers, MatrixStack matrixStack) {
        if(isListening()) onRenderWorld(delta, buffers, matrixStack);
    }

    public void motion() {
        if(isListening()) onMotion();
    }

    protected void onTick() {
    }

    protected void onRenderHud(float delta, Renderer.Buffers buffers, MatrixStack matrixStack) {
    }

    protected void onRenderWorld(float delta, Renderer.Buffers buffers, MatrixStack matrixStack) {
    }

    protected void onMotion() {
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled.getValue();
    }

    public void setEnabled(boolean value) {
        enabled.setValue(value);
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    public int getBind() {
        return bind.getValue();
    }

    public void setBind(int value) {
        bind.setValue(value);
    }

    public boolean isVisible() {
        return visible.getValue();
    }

    public void setVisible(boolean value) {
        visible.setValue(value);
    }

    public TogglesOn getTogglesOn() {
        return togglesOn.getValue();
    }

    public void setTogglesOn(TogglesOn value) {
        togglesOn.setValue(value);
    }

    public boolean isListening() {
        return isEnabled() || defaults.alwaysListening;
    }
}
