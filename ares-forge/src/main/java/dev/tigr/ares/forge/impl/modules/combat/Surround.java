package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.forge.impl.modules.player.Freecam;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "Surround", description = "Surrounds your feet with obsidian", category = Category.COMBAT)
public class Surround extends Module {
    public static Surround INSTANCE;

    private final Setting<Boolean> snap = register(new BooleanSetting("Center", true));
    private BlockPos lastPos = new BlockPos(0, -100, 0);

    public Surround() {
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        if(!MC.player.onGround) return;

        int prevSlot = MC.player.inventory.currentItem;
        int obbyIndex = InventoryUtils.findBlockInHotbar(Blocks.OBSIDIAN);

        if(obbyIndex != -1) {
            EntityPlayer player = Freecam.INSTANCE.getEnabled() ? Freecam.INSTANCE.clone : MC.player;
            if(new BlockPos(player.getPositionVector()).equals(lastPos)) {
                BlockPos[] positions = new BlockPos[]{
                        lastPos.add(0, -1, 1),
                        lastPos.add(1, -1, 0),
                        lastPos.add(0, -1, -1),
                        lastPos.add(-1, -1, 0),
                        lastPos.add(0, 0, 1),
                        lastPos.add(1, 0, 0),
                        lastPos.add(0, 0, -1),
                        lastPos.add(-1, 0, 0),
                };

                for(BlockPos pos: positions) {
                    if(MC.world.getBlockState(pos).getMaterial().isReplaceable()
                            && MC.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))
                            .stream().noneMatch(Entity::canBeCollidedWith)) {
                        MC.player.inventory.currentItem = obbyIndex;
                        WorldUtils.placeBlockMainHand(pos);
                    }
                }

                MC.player.inventory.currentItem = prevSlot;
            } else setEnabled(false);
        }
    }

    @Override
    public void onEnable() {
        BlockPos pos = lastPos = new BlockPos(MC.player.getPositionVector());

        if(snap.getValue()) {
            double xPos = MC.player.getPositionVector().x;
            double zPos = MC.player.getPositionVector().z;

            if(Math.abs((pos.getX() + 0.5) - MC.player.getPositionVector().x) >= 0.2) {
                int xDir = (pos.getX() + 0.5) - MC.player.getPositionVector().x > 0 ? 1 : -1;
                xPos += 0.3 * xDir;
            }

            if(Math.abs((pos.getZ() + 0.5) - MC.player.getPositionVector().z) >= 0.2) {
                int zDir = (pos.getZ() + 0.5) - MC.player.getPositionVector().z > 0 ? 1 : -1;
                zPos += 0.3 * zDir;
            }

            MC.player.motionX = MC.player.motionY = MC.player.motionZ = 0;
            MC.player.setPosition(xPos, pos.getY(), zPos);
            MC.player.connection.sendPacket(new CPacketPlayer.Position(xPos, pos.getY(), zPos, MC.player.onGround));
        }
    }
}
