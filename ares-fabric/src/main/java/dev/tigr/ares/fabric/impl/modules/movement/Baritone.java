package dev.tigr.ares.fabric.impl.modules.movement;

/*import baritone.api.BaritoneAPI;
import baritone.api.event.events.ChatEvent;*/

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;

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

    /*public static <T> void executeCommand(String args) {
        boolean enabled = BaritoneAPI.getSettings().chatControl.value;
        BaritoneAPI.getSettings().chatControl.value = true;

        ChatEvent chatEvent = new ChatEvent(args);
        BaritoneAPI.getProvider().getPrimaryBaritone().getGameEventHandler().onSendChatMessage(chatEvent);
        if(!chatEvent.isCancelled())
            UTILS.printMessage(TextColor.RED + "Invalid Command!\n You can view a list possible commands at " + TextColor.BLUE + "https://github.com/cabaletta/baritone/blob/master/USAGE.md");

        BaritoneAPI.getSettings().chatControl.value = enabled;
    }

    @Override
    public void onTick() {
        BaritoneAPI.getSettings().allowSprint.value = allowSprint.getValue();
        BaritoneAPI.getSettings().allowBreak.value = allowBreak.getValue();
        BaritoneAPI.getSettings().allowParkour.value = allowParkour.getValue();
        BaritoneAPI.getSettings().allowParkourPlace.value = allowParkourPlace.getValue();
        BaritoneAPI.getSettings().allowInventory.value = allowInventory.getValue();
        BaritoneAPI.getSettings().allowDownward.value = allowDownward.getValue();
        BaritoneAPI.getSettings().freeLook.value = freeLook.getValue();
        BaritoneAPI.getSettings().renderGoal.value = renderGoal.getValue();
        BaritoneAPI.getSettings().enterPortal.value = enterPortal.getValue();
    }*/

    @Override
    public void onDisable() {
        setEnabled(true);
    }
}
