package dev.tigr.ares.forge.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "VisualRange", description = "Alerts you when a new player enters your render distance", category = Category.MISC)
public class VisualRange extends Module {
    private final List<EntityPlayer> playerEntities = new ArrayList<>();

    private static String vecToStr(Vec3d vec3d) {
        if(vec3d != null) return WorldUtils.vectorToString(vec3d);
        return "[null]";
    }

    private static String getName(EntityPlayer player) {
        if(player != null) return player.getDisplayNameString();
        return "[null]";
    }

    @Override
    public void onTick() {

        for(EntityPlayer player: MC.world.playerEntities.stream().filter(player -> !player.getName().equals(MC.player.getName())).collect(Collectors.toList())) {
            if(!playerEntities.contains(player)) {
                UTILS.printMessage(TextColor.BLUE + getName(player) + TextColor.GREEN + " entered your render distance at " + vecToStr(player.getPositionVector()));
                playerEntities.add(player);
            }
        }

        for(EntityPlayer player: playerEntities) {
            if(!MC.world.playerEntities.contains(player)) {
                UTILS.printMessage(TextColor.BLUE + getName(player) + TextColor.RED + " left your render distance at " + vecToStr(player.getPositionVector()));
                playerEntities.remove(player);
            }
        }
    }
}
