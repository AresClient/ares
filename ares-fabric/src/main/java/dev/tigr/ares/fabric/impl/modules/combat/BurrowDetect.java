package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.TextColor;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Hoosiers, 11/25/2020
 */

@Module.Info(name = "BurrowDetect", description = "Sends a message in chat whenever a player is burrowed", category = Category.COMBAT)
public class BurrowDetect extends Module {

    private final List<PlayerEntity> burrowedPlayers = new ArrayList<>();

    @Override
    public void onTick(){

        for (PlayerEntity playerEntity : MC.world.getPlayers().stream().filter(entityPlayer -> entityPlayer != MC.player).collect(Collectors.toList())){
            if (!burrowedPlayers.contains(playerEntity) && isInBurrow(playerEntity)){
                UTILS.printMessage(TextColor.BLUE + playerEntity.getEntityName() + TextColor.GREEN + " has burrowed");
                burrowedPlayers.add(playerEntity);
            }
        }

        for (PlayerEntity playerEntity : burrowedPlayers){
            if (!isInBurrow(playerEntity)){
                UTILS.printMessage(TextColor.BLUE + playerEntity.getEntityName() + TextColor.RED + " is no longer burrowed");
                burrowedPlayers.remove(playerEntity);
            }
        }
    }

    private boolean isInBurrow(PlayerEntity playerEntity){
        BlockPos playerPos = new BlockPos(getMiddlePosition(playerEntity.getX()), playerEntity.getY(), getMiddlePosition(playerEntity.getZ()));

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