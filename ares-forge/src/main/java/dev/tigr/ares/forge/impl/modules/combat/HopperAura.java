package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "HopperAura", description = "Break nearby hoppers", category = Category.COMBAT)
public class HopperAura extends Module {
    private final Set<BlockPos> hoppersPlaced = new HashSet<BlockPos>() {
    };
    private final int[] picks = {278, 285, 274, 270, 257};
    private final Setting<Double> distance = register(new DoubleSetting("Distance", 5.0D, 1.0D, 10.0D));
    private final Setting<Boolean> lockRotations = register(new BooleanSetting("LockRotations", false));
    private final Setting<Boolean> breakOwn = register(new BooleanSetting("Break Own", false));
    @EventHandler
    public EventListener<PlayerInteractEvent.RightClickBlock> rightClickBlockEvent = new EventListener<>(event -> {
        if(MC.player.inventory.getStackInSlot(MC.player.inventory.currentItem).getItem().equals(Item.getItemById(154))) {
            hoppersPlaced.add(MC.objectMouseOver.getBlockPos().offset(MC.objectMouseOver.sideHit));
        }
    });

    @Override
    public void onTick() {
        List<TileEntity> hoppers = MC.world.loadedTileEntityList.stream().filter(p -> p instanceof TileEntityHopper).collect(Collectors.toList());

        if(hoppers.size() > 0) {
            for(TileEntity hopper: hoppers) {
                BlockPos hopperPos = hopper.getPos();

                //Dont break own hoppers
                if(!breakOwn.getValue() && hoppersPlaced.contains(hopperPos)) continue;

                if(MC.player.getDistance(hopperPos.getX(), hopperPos.getY(), hopperPos.getZ()) <= distance.getValue()) {
                    for(int x: picks) {
                        int slot = InventoryUtils.findItemInHotbar(Item.getItemById(x));
                        if(slot != -1) {
                            MC.player.inventory.currentItem = slot;

                            if(lockRotations.getValue()) WorldUtils.lookAtBlock(hopperPos);

                            MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, hopper.getPos(), EnumFacing.UP));
                            MC.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, hopper.getPos(), EnumFacing.UP));
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        hoppersPlaced.clear();
    }
}
