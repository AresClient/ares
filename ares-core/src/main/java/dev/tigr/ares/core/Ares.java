package dev.tigr.ares.core;

import dev.tigr.ares.core.event.client.PostInitializationEvent;
import dev.tigr.ares.core.feature.AccountManager;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.global.Tracker;
import dev.tigr.ares.core.util.tracker.HotbarTracker;
import dev.tigr.ares.core.util.interfaces.IEntity;
import dev.tigr.ares.core.util.interfaces.IInv;
import dev.tigr.ares.core.util.interfaces.IPacket;
import dev.tigr.ares.core.util.interfaces.ISelf;
import dev.tigr.ares.core.setting.SettingsManager;
import dev.tigr.ares.core.util.IGUIManager;
import dev.tigr.ares.core.util.IKeyboardManager;
import dev.tigr.ares.core.util.IUtils;
import dev.tigr.ares.core.util.render.IRenderStack;
import dev.tigr.ares.core.util.render.IRenderer;
import dev.tigr.ares.core.util.render.ITextureManager;
import dev.tigr.ares.core.util.render.font.AbstractFontRenderer;
import dev.tigr.ares.core.util.render.font.GlyphFont;
import dev.tigr.ares.core.util.tracker.RotationTracker;
import dev.tigr.simpleevents.EventManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigermouthbear 11/5/20
 */
public abstract class Ares {
    public static final Logger LOGGER = LogManager.getLogger("Ares");
    public static final EventManager EVENT_MANAGER = new EventManager();
    public static final String MODID = "ares";
    public static final String NAME = "Ares";

    public enum Branches { BETA, STABLE }
    public static String MOD_LOADER;
    public static String MC_VERSION;
    public static String VERSION;
    public static Branches BRANCH;
    public static String VERSION_FULL;

    public static IUtils UTILS = null;
    public static IGUIManager GUI_MANAGER = null;
    public static IKeyboardManager KEYBOARD_MANAGER = null;
    public static IRenderer RENDERER = null;
    public static IRenderStack RENDER_STACK = null;
    public static AbstractFontRenderer FONT_RENDERER = null;
    public static ITextureManager TEXTURE_MANAGER = null;

    public static ISelf SELF = null;
    public static IInv INV = null;
    public static IPacket PACKET = null;
    public static IEntity ENTITY = null;

    public static final HotbarTracker HOTBAR_TRACKER = Tracker.addTracker(new HotbarTracker());
    public static final RotationTracker ROTATIONS = Tracker.addTracker(new RotationTracker());

    public static final GlyphFont MONO_FONT = new GlyphFont("/assets/ares/font/mono.ttf", 64);
    public static final GlyphFont ARIAL_FONT = new GlyphFont("/assets/ares/font/arial.ttf", 64);

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Info {
        String modLoader();

        String minecraftVersion();

        String version();

        Branches branch();
    }

    public static void initialize(Class<? extends Ares> clazz) throws RuntimeException {
        try {
            Ares ares = clazz.newInstance();

            // get and set ver info
            // should not be null
            Info info = clazz.getAnnotation(Info.class);
            MOD_LOADER = info.modLoader();
            MC_VERSION = info.minecraftVersion();
            VERSION = info.version();
            BRANCH = info.branch();
            VERSION_FULL = BRANCH == Branches.STABLE ? VERSION : VERSION.concat(" BETA");

            // make sure required implementations are present
            if(UTILS == null) error("Missing Utils! Shutting down...");
            else if(GUI_MANAGER == null) error("Missing GuiManager! Shutting down...");
            else if(KEYBOARD_MANAGER == null) error("Missing KeyboardManager! Shutting down...");
            else if(RENDERER == null) error("Missing renderer! Shutting down...");
            else if(RENDER_STACK == null) error("Missing renderStack! Shutting down...");
            else if(FONT_RENDERER == null) error("Missing fontRenderer! Shutting down...");
            else if(TEXTURE_MANAGER == null) error("Missing textureManager! Shutting down...");

            else if(SELF == null) error("Missing Self! Shutting down...");
            else if(INV == null) error("Missing Inv! Shutting down...");
            else if(PACKET == null) error("Missing Packet! Shutting down...");
            else if(ENTITY == null) error("Missing Entity! Shutting down...");

            // complete initialization
            ares.initMain();
        } catch(IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    private static void error(String message) throws RuntimeException {
        LOGGER.error(message);
        throw new RuntimeException("[Ares] " + message);
    }

    public static void save() {
        SettingsManager.save();
        AccountManager.save();
        FriendManager.save();
    }

    protected void initMain() {
        LOGGER.info("\n" +
                "_____________________________________________\n" +
                "|   $$$$                                    |\n" +
                "|  $$  $$                                   |\n" +
                "| $$    $$     $$$$$$$   $$$$$$$$    $$$$$$$|\n" +
                "|$$      $$    $$        $$    $$   $$      |\n" +
                "|$$      $$    $$        $$$$$$$$   $$$$$$$$|\n" +
                "|$$      $$    $$        $$               $$|\n" +
                "|$$      $$    $$        $$$$$$$$   $$$$$$$ |\n" +
                "|___________________________________________|\n");
        LOGGER.info("Loading Ares...");

        long startTime = System.currentTimeMillis();

        Tracker.getTrackers().forEach(Tracker::registerTrackers);

        initModules();
        initCommands();

        SettingsManager.read();
        FriendManager.read();

        EVENT_MANAGER.post(new PostInitializationEvent());

        LOGGER.info("Ares Client loaded in " + (System.currentTimeMillis() - startTime) + " milliseconds");
    }

    private void initModules() {
        List<Class<? extends Module>> modules = getModules();
        ArrayList<Class<? extends Module>> failedModules = Module.MANAGER.initialize(modules).getErroredClasses();
        if(failedModules.size() > 0) {
            LOGGER.info("Ares Client attempted to load " + Module.MANAGER.getInstances().size() + " modules, out of which " + failedModules.size() + " failed to load");
            LOGGER.info("Failed Modules: " + failedModules.toString());
        } else {
            LOGGER.info(modules.size() + " modules loaded successfully");
        }

        // setup the events for each module
        Module.MANAGER.getInstances().forEach(Module::setupEvents);
    }

    protected abstract List<Class<? extends Module>> getModules();

    private void initCommands() {
        List<Class<? extends Command>> commands = getCommands();
        ArrayList<Class<? extends Command>> failedCommands = Command.MANAGER.initialize(commands).getErroredClasses();
        if(failedCommands.size() > 0) {
            LOGGER.info("Ares Client attempted to load " + Command.MANAGER.getInstances().size() + " commands, out of which " + failedCommands.size() + " failed to load");
            LOGGER.info("Failed Commands: " + failedCommands.toString());
        } else {
            LOGGER.info(commands.size() + " commands loaded successfully");
        }
    }

    protected abstract List<Class<? extends Command>> getCommands();
}
