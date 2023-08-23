package org.aresclient.ares.api;

import dev.tigr.simpleevents.EventManager;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import kotlin.Unit;
import kotlinx.serialization.json.JsonElement;
import net.meshmc.mesh.loader.MeshLoader;
import net.meshmc.mesh.loader.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aresclient.ares.AresStatics;
import org.aresclient.ares.api.command.Command;
import org.aresclient.ares.api.event.AresEvent;
import org.aresclient.ares.api.event.AresEventManager;
import org.aresclient.ares.api.event.client.InputEvent;
import org.aresclient.ares.api.event.client.ShutdownEvent;
import org.aresclient.ares.api.event.client.TickEvent;
import org.aresclient.ares.api.event.render.RenderEvent;
import org.aresclient.ares.api.global.Global;
import org.aresclient.ares.api.minecraft.Minecraft;
import org.aresclient.ares.api.module.Module;
import org.aresclient.ares.api.setting.Setting;
import org.aresclient.ares.impl.JsonSettingSerializer;
import org.aresclient.ares.api.render.MatrixStack;
import org.aresclient.ares.api.render.Renderer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Ares {
    public static class Plugin {
        private String id;
        private String name;
        private String description;
        private String version;
        private String[] authors;

        private final List<Global> globals = new ArrayList<>();
        private final List<Module> modules = new ArrayList<>();
        private final List<Command> commands = new ArrayList<>();
        private Setting.Map<?> settings;
        private Logger logger;

        public void init(String id, String name, String description, String version, String[] authors) {
            long start = System.currentTimeMillis();

            this.id = id;
            this.name = name;
            this.description = description;
            this.version = version;
            this.authors = authors;

            settings = PLUGIN_SETTINGS.addMap(id);
            logger = LogManager.getLogger(name);

            PLUGINS.add(this);

            init();

            for(Global global: globals) {
                Ares.getEventManager().register(global);
                Ares.getEventManager().register(global.getClass());
            }

            for(Module module: modules) {
                if(module.isListening()) {
                    Ares.getEventManager().register(module);
                    Ares.getEventManager().register(module.getClass());
                }
                MeshLoader.getInstance().registerInterfaces(module);
            }

            logger.info("Loaded {} globals, {} modules and {} commands in {} milliseconds",
                globals.size(), modules.size(), commands.size(), System.currentTimeMillis() - start);
        }

        public void init(Mod mod) {
            init(mod.getId(), mod.getName(), mod.getDescription(), mod.getVersion(), mod.getAuthors());
        }

        public void init() {
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getVersion() {
            return version;
        }

        public String[] getAuthors() {
            return authors;
        }

        public List<Global> getGlobals() {
            return globals;
        }

        public List<Module> getModules() {
            return modules;
        }

        public List<Command> getCommands() {
            return commands;
        }

        public Setting.Map<?> getSettings() {
            return settings;
        }

        public Logger getLogger() {
            return logger;
        }
    }

    private static final List<Plugin> PLUGINS = new ArrayList<>();
    private static final EventManager EVENT_MANAGER = new AresEventManager();

    private static final File SETTINGS_FILE = new File("ares/config/settings.json");
    private static final JsonSettingSerializer SETTINGS_SERIALIZER = new JsonSettingSerializer(settings -> {
        settings.setPrettyPrint(true);
        return Unit.INSTANCE; // kotlin moment
    });
    private static final Setting.Map<JsonElement> SETTINGS = SETTINGS_SERIALIZER.read(SETTINGS_FILE);
    private static final Setting.Map<?> PLUGIN_SETTINGS = SETTINGS.addMap("Plugins");

    private static Minecraft MINECRAFT = null;
    public static Minecraft getMinecraft() {
        if(MINECRAFT == null) MINECRAFT = AresStatics.getMinecraft();
        return MINECRAFT;
    }

    public static List<Plugin> getPlugins() {
        return PLUGINS;
    }

    public static EventManager getEventManager() {
        return EVENT_MANAGER;
    }

    public static Setting.Map<?> getSettings() {
        return SETTINGS;
    }

    public static void tickClient() {
        for(Plugin plugin: PLUGINS) {
            plugin.getGlobals().forEach(Global::tick);
            plugin.getModules().forEach(Module::tick);
        }
    }

    public static void renderHud(float delta, Renderer.Buffers buffers, MatrixStack matrixStack) {
        for(Plugin plugin: PLUGINS) {
            for(Module module: plugin.getModules()) {
                module.renderHud(delta, buffers, matrixStack);
            }
        }
    }

    public static void renderWorld(float delta, Renderer.Buffers buffers, MatrixStack matrixStack) {
        for(Plugin plugin: PLUGINS) {
            for(Module module: plugin.getModules()) {
                module.renderWorld(delta, buffers, matrixStack);
            }
        }
    }

    public static void tickMotion() {
        for(Plugin plugin: PLUGINS) plugin.getModules().forEach(Module::motion);
    }

    @EventHandler
    private static final EventListener<ShutdownEvent> shutdownListener = new EventListener<>(event -> {
        SETTINGS_FILE.mkdirs();
        SETTINGS_SERIALIZER.write(SETTINGS, SETTINGS_FILE);
    });

    @EventHandler
    private static final EventListener<TickEvent> tickEventListener = new EventListener<>(event -> {
        if(event.getEra() != AresEvent.Era.BEFORE) return;
        if(event.getType() == TickEvent.Type.CLIENT) tickClient();
        else if(event.getType() == TickEvent.Type.MOTION) tickMotion();
    });

    @EventHandler
    private static final EventListener<RenderEvent> renderEventListener = new EventListener<>(event -> {
        if(event.getType() == RenderEvent.Type.HUD) {
            Renderer.State state = Renderer.begin2d();
            renderHud(event.getTickDelta(), state.getBuffers(), state.getMatrixStack());
            Renderer.end(state);
        } else if(event.getType() == RenderEvent.Type.WORLD) {
            Renderer.State state = Renderer.begin3d();
            renderWorld(event.getTickDelta(), state.getBuffers(), state.getMatrixStack());
            Renderer.end(state);
        }
    });

    @EventHandler
    private static final EventListener<InputEvent> inputEventListener = new EventListener<>(event -> {
        int key;
        boolean state;

        if(event instanceof InputEvent.Keyboard.Pressed) {
            key = ((InputEvent.Keyboard.Pressed) event).getKey();
            state = true;
        } else if(event instanceof InputEvent.Keyboard.Released) {
            key = ((InputEvent.Keyboard.Released) event).getKey();
            state = false;
        } else if(event instanceof InputEvent.Mouse.Pressed) {
            key = ((InputEvent.Mouse.Pressed) event).getKey();
            state = true;
        } else if(event instanceof InputEvent.Mouse.Released) {
            key = ((InputEvent.Mouse.Released) event).getKey();
            state = false;
        } else return;

        for(Setting.Bind bind: Setting.Bind.getAll()) {
            if(bind.getValue() != key) continue;
            bind.getCallback().accept(state);
        }
    });

    // TODO: COMMAND LISTENERS

    static {
        getEventManager().register(Ares.class);
    }
}
