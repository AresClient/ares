package dev.tigr.ares.forge.impl.modules.misc;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.MouseEvent;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "MidClickFriend", description = "Allows you to add and remove people from friends list using middle click", category = Category.MISC)
public class MidClickFriend extends Module {
    @EventHandler
    public EventListener<MouseEvent> mouseClickEvent = new EventListener<>(event -> {
        if(event.getButton() == 2 && event.isButtonstate()) {
            if(MC.objectMouseOver != null && MC.objectMouseOver.entityHit != null && MC.objectMouseOver.entityHit instanceof EntityPlayer) {
                EntityPlayer ep = (EntityPlayer) MC.objectMouseOver.entityHit;
                if(FriendManager.isFriend(ep.getGameProfile().getName())) {
                    FriendManager.removeFriend(ep.getName());
                    UTILS.printMessage(TextColor.RED + "Removed " + TextColor.BLUE + ep.getName() + TextColor.RED + " from your friends list!");
                } else {
                    FriendManager.addFriend(ep.getGameProfile().getName());
                    UTILS.printMessage(TextColor.GREEN + "Added " + TextColor.BLUE + ep.getName() + TextColor.GREEN + " to your friends list!");
                }
            }
        }
    });
}
