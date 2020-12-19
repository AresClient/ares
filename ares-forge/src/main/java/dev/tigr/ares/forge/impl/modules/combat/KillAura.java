package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.setting.settings.numerical.DoubleSetting;
import dev.tigr.ares.core.setting.settings.numerical.IntegerSetting;
import dev.tigr.ares.forge.utils.Comparators;
import dev.tigr.ares.forge.utils.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;

import java.util.List;

/**
 * @author Tigermouthbear
 * updated by Tigermouthbear 12/19/20
 */
@Module.Info(name = "KillAura", description = "Automatically hit nearby players", category = Category.COMBAT)
public class KillAura extends Module {
    private final Setting<Double> range = register(new DoubleSetting("Range", 5.0D, 1.0D, 15.0D));
    private final Setting<Boolean> superOnly = register(new BooleanSetting("32k Only", false));
    private final Setting<Boolean> autoDelay = register(new BooleanSetting("Auto Delay", true));
    private final Setting<Integer> delay = register(new IntegerSetting("Delay in ticks", 40, 0, 50)).setVisibility(() -> !autoDelay.getValue());
    private final Setting<Boolean> weaponOnly = register(new BooleanSetting("Weapon Only", true));

    private final Setting<Boolean> players = register(new BooleanSetting("Players", true));
    private final Setting<Boolean> friends = register(new BooleanSetting("Friends", false)).setVisibility(players::getValue);
    private final Setting<Boolean> teammates = register(new BooleanSetting("Teammates", false)).setVisibility(players::getValue);
    private final Setting<Boolean> passive = register(new BooleanSetting("Passive", false));
    private final Setting<Boolean> hostile = register(new BooleanSetting("Hostile", true));
    private final Setting<Boolean> nametagged = register(new BooleanSetting("Nametagged", true));
    private final Setting<Boolean> bots = register(new BooleanSetting("Bots", false));

    private int hasWaited = 0;

    private boolean itemInHand() {
        return !weaponOnly.getValue() || (MC.player.getHeldItemMainhand().getItem() instanceof ItemAxe || MC.player.getHeldItemMainhand().getItem() instanceof ItemSword);
    }

    @Override
    public void onTick() {
        if(MC.player.isDead || MC.world == null || !itemInHand()) return;

        if(autoDelay.getValue()) {
            if((int) (MC.player.getCooledAttackStrength(0.0F) * 17.0F) < 16) return;
        } else {
            if(hasWaited < delay.getValue()) {
                hasWaited++;
                return;
            }

            hasWaited = 0;
        }

        List<Entity> targets = WorldUtils.getTargets(players.getValue(), friends.getValue(), teammates.getValue(), passive.getValue(), hostile.getValue(), nametagged.getValue(), bots.getValue());
        targets.sort(Comparators.entityDistance);

        for(Entity entity: targets) {
            // if the target is in range and
            if(MC.player.getDistance(entity) <= range.getValue() && ((EntityLivingBase) entity).getHealth() > 0
            && (Auto32k.isSuperWeapon(MC.player.getHeldItemMainhand()) || !superOnly.getValue())) {
                MC.playerController.attackEntity(MC.player, entity);
                MC.player.swingArm(EnumHand.MAIN_HAND);
                return;
            }
        }
    }
}
