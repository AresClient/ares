package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Hoosiers, 11/25/2020
 */

@Module.Info(name = "BurrowDetect", description = "Sends a message in chat whenever a player is burrowed", category = Category.COMBAT)
public class BurrowDetect extends Module {

    private final List<EntityPlayer> burrowedPlayers = new ArrayList<>();

    @Override
    public void onTick(){

        for (EntityPlayer entityPlayer : MC.world.playerEntities.stream().filter(entityPlayer -> entityPlayer != MC.player).collect(Collectors.toList())){
            if (!burrowedPlayers.contains(entityPlayer) && isInBurrow(entityPlayer)){
                UTILS.printMessage(TextColor.BLUE + entityPlayer.getDisplayNameString() + TextColor.GREEN + " has burrowed");
                burrowedPlayers.add(entityPlayer);
            }
        }

        for (EntityPlayer entityPlayer : burrowedPlayers){
            if (!isInBurrow(entityPlayer)){
                UTILS.printMessage(TextColor.BLUE + entityPlayer.getDisplayNameString() + TextColor.RED + " is no longer burrowed");
                burrowedPlayers.remove(entityPlayer);
            }
        }
    }

    private boolean isInBurrow(EntityPlayer entityPlayer){
        BlockPos pos = new BlockPos(getMiddlePosition(entityPlayer.posX), entityPlayer.posY, getMiddlePosition(entityPlayer.posZ));
        BlockPos playerPos = WorldUtils.roundBlockPos(new Vec3d(pos.getX(), entityPlayer.posY, pos.getZ()));

        return MC.world.getBlockState(playerPos).getBlock() == Blocks.OBSIDIAN
                || MC.world.getBlockState(playerPos).getBlock() == Blocks.ENDER_CHEST
                || MC.world.getBlockState(playerPos).getBlock() == Blocks.ANVIL;
    }

    //This converts a double position such as 12.9 or 12.13 to a "middle" value of 12.5
    private double getMiddlePosition(double positionIn){
        double positionFinal = Math.round(positionIn);

        if (Math.round(positionIn) > positionIn){
            positionFinal -= 0.5;
        }
        else if (Math.round(positionIn) <= positionIn){
            positionFinal += 0.5;
        }

        return positionFinal;
    }
}