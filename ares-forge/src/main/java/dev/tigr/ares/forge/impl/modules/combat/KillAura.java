package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.forge.utils.Comparators;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

import java.util.List;

/**
 * @authors Tigermouthbear
 */
@Module.Info(name = "KillAura", description = "Automatically hit nearby players", category = Category.COMBAT)
public class KillAura extends Module {
    private final Setting<Double> range = register(new DoubleSetting("Range", 5.0D, 1.0D, 15.0D));
    private final Setting<Boolean> superOnly = register(new BooleanSetting("32k Only", false));
    private final Setting<Boolean> playersOnly = register(new BooleanSetting("Players only", true));
    private final Setting<Boolean> autoDelay = register(new BooleanSetting("Auto Delay", true));
    private final Setting<Integer> delay = register(new IntegerSetting("Delay in ticks", 40, 0, 50)).setVisibility(() -> !autoDelay.getValue());

    private int hasWaited = 0;

    @Override
    public void onTick() {
        if(MC.player.isDead || MC.world == null) return;

        if(autoDelay.getValue()) {
            if((int) (MC.player.getCooledAttackStrength(0.0F) * 17.0F) < 16) return;
        } else {
            if(hasWaited < delay.getValue()) {
                hasWaited++;
                return;
            }

            hasWaited = 0;
        }

        List<Entity> targets = MC.world.loadedEntityList;
        targets.sort(Comparators.entityDistance);

        for(Entity entity: MC.world.loadedEntityList) {
            // Only hit living entities that are not the own player
            if(!(entity instanceof EntityLivingBase) || entity == MC.player) continue;

            // If the target is in range
            if(MC.player.getDistance(entity) <= range.getValue()) {
                if(((EntityLivingBase) entity).getHealth() > 0) {
                    if(entity instanceof EntityPlayer || !playersOnly.getValue()) {
                        if(!(entity instanceof EntityPlayer) || !FriendManager.isFriend(((EntityPlayer) entity).getGameProfile().getName())) {
                            if(Auto32k.isSuperWeapon(MC.player.getHeldItemMainhand()) || !superOnly.getValue()) {
                                MC.playerController.attackEntity(MC.player, entity);
                                MC.player.swingArm(EnumHand.MAIN_HAND);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
