package dev.tigr.ares.fabric.impl.modules.combat;

import com.google.common.collect.Iterables;
import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.fabric.utils.Comparators;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @authors Tigermouthbear
 */
@Module.Info(name = "KillAura", description = "Automatically hit nearby players", category = Category.COMBAT)
public class KillAura extends Module {
    private static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    private final Setting<Double> range = register(new DoubleSetting("Range", 5.0D, 1.0D, 15.0D));
    private final Setting<Boolean> superOnly = register(new BooleanSetting("32k Only", false));
    private final Setting<Boolean> playersOnly = register(new BooleanSetting("Players only", true));
    private final Setting<Boolean> autoDelay = register(new BooleanSetting("Auto Delay", true));
    private final Setting<Integer> delay = register(new IntegerSetting("Delay in ticks", 40, 0, 50)).setVisibility(() -> !autoDelay.getValue());

    private int hasWaited = 0;

    @Override
    public void onTick() {
        if(MC.player.isDead() || MC.world == null) return;

        if(autoDelay.getValue()) {
            if((int) (MC.player.getAttackCooldownProgress(0.0F) * 17.0F) < 16) return;
        } else {
            if(hasWaited < delay.getValue()) {
                hasWaited++;
                return;
            }

            hasWaited = 0;
        }

        List<Entity> targets = Arrays.asList(Iterables.toArray(MC.world.getEntities(), Entity.class));
        targets.sort(Comparators.entityDistance);

        for(Entity entity: MC.world.getEntities()) {
            // Only hit living entities that are not the own player
            if(!(entity instanceof LivingEntity) || entity == MC.player) continue;

            // If the target is in range
            if(MC.player.distanceTo(entity) <= range.getValue()) {
                if(((LivingEntity) entity).getHealth() > 0) {
                    if(entity instanceof PlayerEntity || !playersOnly.getValue()) {
                        if(!(entity instanceof PlayerEntity) || !FriendManager.isFriend(((PlayerEntity) entity).getGameProfile().getName())) {
                            MC.interactionManager.attackEntity(MC.player, entity);
                            MC.player.swingHand(Hand.MAIN_HAND);
                            return;
                        }
                    }
                }
            }
        }
    }
}
