package org.aresclient.ares.api.instrument.module;

import org.aresclient.ares.api.Ares;
import org.aresclient.ares.api.render.Texture;
import org.aresclient.ares.api.setting.Setting;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Category {
    private static final Setting.Map<?> SETTINGS = Ares.getSettings().addMap("Modules");
    private static final List<Category> CATEGORIES = new ArrayList<>();

    public static final Category PLAYER = new Category("Player");
    public static final Category OFFENSE = new Category("Offense");
    public static final Category DEFENSE = new Category("Defense");
    public static final Category MOVEMENT = new Category("Movement");
    public static final Category RENDER = new Category("Render");
    public static final Category HUD = new Category("Hud");
    public static final Category MISC = new Category("Misc");

    private final String name;
    private final InputStream icon;
    private final Setting.Map<?> settings;
    private final List<Module> modules = new ArrayList<>();

    // lazy load this because it needs to be created on a render thread
    private Texture iconTexture = null;

    public Category(String name, InputStream icon) {
        this.name = name;
        this.icon = icon;
        this.settings = SETTINGS.addMap(name);
        CATEGORIES.add(this);
    }

    public Category(String name) {
        this(name, Category.class.getResourceAsStream(
            "/assets/ares/textures/icons/categories/" + name.toLowerCase() + ".png"));
    }

    public String getName() {
        return name;
    }

    public Texture getIcon() {
        if(iconTexture == null) iconTexture = new Texture(icon, false);
        return iconTexture;
    }

    public Setting.Map<?> getSettings() {
        return settings;
    }

    public List<Module> getModules() {
        return modules;
    }

    public static List<Category> getAll() {
        return CATEGORIES;
    }
}
