package dev.tigr.ares.core.feature.module;

import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.client.ToggleEvent;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import dev.tigr.ares.core.setting.settings.BindSetting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.global.Manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;

/**
 * @author Tigermouthbear
 */
public class Module implements Wrapper {
    private static final SettingCategory SETTING_CATEGORY = new SettingCategory("Hacks");
    public static final Manager<Module> MANAGER = new Manager<>();

    private final String name = getAnnotation().name();
    private final String description = getAnnotation().description();
    private final Category category = getAnnotation().category().add(this);
    private final boolean alwaysListening = getAnnotation().alwaysListening();

    // settings
    private final SettingCategory settingCategory = new SettingCategory(SETTING_CATEGORY, name);
    private final Setting<Boolean> enabled = register(new BooleanSetting("Enabled", getAnnotation().enabled()));
    private final Setting<Boolean> isVisible = register(new BooleanSetting("Is Visible", getAnnotation().visible()));
    private final Setting<String> bind = register(new BindSetting("Bind", getAnnotation().bind(), setting -> toggle()));

    public void setupEvents() {
        if(enabled.getValue() || alwaysListening) Ares.EVENT_MANAGER.register(this);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Info {
        String name();

        String description();

        Category category();

        boolean enabled() default false;

        boolean visible() default true;

        String bind() default "NONE";

        boolean alwaysListening() default false;
    }

    private Info getAnnotation() {
        return getClass().getAnnotation(Info.class);
    }

    protected String getInfo() {
        return "";
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean getEnabled() {
        return enabled.getValue();
    }

    public void setEnabled(boolean value) {
        enabled.setValue(value);
        if(value) {
            onEnable();
            if(!alwaysListening) Ares.EVENT_MANAGER.register(this);
            Ares.EVENT_MANAGER.post(new ToggleEvent(this, true));
        }
        if(!value) {
            onDisable();
            if(!alwaysListening) Ares.EVENT_MANAGER.unregister(this);
            Ares.EVENT_MANAGER.post(new ToggleEvent(this, false));
        }
    }

    public void toggle() {
        setEnabled(!getEnabled());
    }

    public String getHudName() {
        return getName() + (getInfo().equals("") ? "" : "\u00a78 [" + getInfo() + "]");
    }

    public boolean isVisible() {
        return isVisible.getValue();
    }

    public boolean isAlwaysListening() {
        return alwaysListening;
    }

    public ArrayList<Setting<?>> getSettings() {
        ArrayList<Setting<?>> list = new ArrayList<>();
        for(Setting<?> setting: settingCategory.getSettings()) {
            if(setting != enabled) list.add(setting);
        }
        return list;
    }

    public Setting<String> getBind() {
        return bind;
    }

    protected <T extends Setting<?>> T register(T setting) {
        setting.setParent(settingCategory);
        return setting;
    }

    // events
    public void onTick() {
    }

    public void onRender2d() {
    }

    public void onRender3d() {
    }

    public void onMotion() {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    protected static long TICKS = 0;
    public static void tick() {
        try {
            TICKS++;
            for(Module module: Module.MANAGER.getInstances()) {
                if(module.getEnabled() || module.isAlwaysListening()) {
                    module.onTick();
                }
            }
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    public static void render3d() {
        try {
            for(Module module: Module.MANAGER.getInstances()) {
                if(module.getEnabled() || module.isAlwaysListening()) {
                    module.onRender3d();
                }
            }
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    public static void render2d() {
        try {
            for(Module module: Module.MANAGER.getInstances()) {
                if(module.getEnabled() || module.isAlwaysListening()) {
                    module.onRender2d();
                }
            }
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    public static void motion() {
        try {
            for(Module module: Module.MANAGER.getInstances()) {
                if(module.getEnabled() || module.isAlwaysListening()) {
                    module.onMotion();
                }
            }
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
}
