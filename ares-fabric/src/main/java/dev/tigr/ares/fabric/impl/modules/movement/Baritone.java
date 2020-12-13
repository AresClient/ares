package dev.tigr.ares.fabric.impl.modules.movement;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.event.events.ChatEvent;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Baritone", description = "Allows you to change the settings for baritone", category = Category.MOVEMENT, enabled = true, visible = false, alwaysListening = true)
public class Baritone extends Module {
    private final Setting<Boolean> allowSprint = register(new BooleanSetting("Allow Sprint", true));
    private final Setting<Boolean> allowBreak = register(new BooleanSetting("Allow Break", true));
    private final Setting<Boolean> allowParkour = register(new BooleanSetting("Allow Parkour", true));
    private final Setting<Boolean> allowParkourPlace = register(new BooleanSetting("Allow Parkour Place", true));
    private final Setting<Boolean> allowInventory = register(new BooleanSetting("Manage Inventory", false));

    private final Setting<Boolean> allowDownward = register(new BooleanSetting("Allow Downward", true));
    private final Setting<Boolean> freeLook = register(new BooleanSetting("Freelook", true));
    private final Setting<Boolean> renderGoal = register(new BooleanSetting("Render Goal", true));
    private final Setting<Boolean> enterPortal = register(new BooleanSetting("Avoid Portals", false));

    private static <T> void setVal(Settings.Setting<T> setting, T val) {
        ReflectionHelper.setPrivateValue(Settings.Setting.class, setting, val, "value");
    }

    private static <T> boolean getVal(Settings.Setting<T> setting) {
        return ReflectionHelper.getPrivateValue(Settings.Setting.class, setting, "value");
    }

    public static <T> void executeCommand(String args) {
        boolean enabled = getVal(BaritoneAPI.getSettings().chatControl);
        setVal(BaritoneAPI.getSettings().chatControl, true);

        ChatEvent chatEvent = new ChatEvent(args);
        BaritoneAPI.getProvider().getPrimaryBaritone().getGameEventHandler().onSendChatMessage(chatEvent);
        if(!chatEvent.isCancelled())
            UTILS.printMessage("Invalid Command! You can view a list possible commands at https://github.com/cabaletta/baritone/blob/master/USAGE.md");

        setVal(BaritoneAPI.getSettings().chatControl, enabled);
    }

    @Override
    public void onTick() {
        setVal(BaritoneAPI.getSettings().allowSprint, allowSprint.getValue());
        setVal(BaritoneAPI.getSettings().allowBreak, allowBreak.getValue());
        setVal(BaritoneAPI.getSettings().allowParkour, allowParkour.getValue());
        setVal(BaritoneAPI.getSettings().allowParkourPlace, allowParkourPlace.getValue());
        setVal(BaritoneAPI.getSettings().allowInventory, allowInventory.getValue());
        setVal(BaritoneAPI.getSettings().allowDownward, allowDownward.getValue());
        setVal(BaritoneAPI.getSettings().freeLook, freeLook.getValue());
        setVal(BaritoneAPI.getSettings().renderGoal, renderGoal.getValue());
        setVal(BaritoneAPI.getSettings().enterPortal, enterPortal.getValue());
    }

    @Override
    public void onDisable() {
        setEnabled(true);
    }
}
