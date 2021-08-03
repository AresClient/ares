package dev.tigr.ares.fabric.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.Pair;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.player.DamageBlockEvent;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.math.BlockPos;

/**
 * @author Tigermouthbear
 * ported to Fabric by Makrennel 5/13/21
 */
@Module.Info(name = "AutoTool", description = "Automatically picks the best tool for the job", category = Category.MISC)
public class AutoTool extends Module {
    private final Setting<Boolean> endCrystals = register(new BooleanSetting("End Crystals", false));

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof PlayerInteractEntityC2SPacket) {
            Pair<WorldUtils.InteractType, Integer> interactData = WorldUtils.getInteractData((PlayerInteractEntityC2SPacket) event.getPacket());
            if(interactData.getFirst() == WorldUtils.InteractType.ATTACK) {
                if(MC.world.getEntityById(interactData.getSecond()) instanceof EndCrystalEntity && !endCrystals.getValue()) return;
                int slot = getWeapon();
                if(slot != -1 && slot != MC.player.getInventory().selectedSlot) {
                    MC.player.getInventory().selectedSlot = slot;
                    MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(MC.player.getInventory().selectedSlot));
                }
            }
        }
    });

    @EventHandler
    public EventListener<DamageBlockEvent> leftClickBlockEvent = new EventListener<>(event -> {
        int slot = getTool(event.getBlockPos());
        if(slot != -1 && slot != MC.player.getInventory().selectedSlot) {
            MC.player.getInventory().selectedSlot = slot;
            MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(MC.player.getInventory().selectedSlot));
        }
    });

    private int getWeapon() {
        int index = -1;
        double best = 0;
        for(int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if(stack.isEmpty()) continue;
            double damage = -1;
            Item item = stack.getItem();
            if(item instanceof MiningToolItem)
                damage = ((MiningToolItem) item).getAttackDamage() + (double) EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT);
            if(item instanceof SwordItem)
                damage = ((SwordItem) item).getAttackDamage() + (double) EnchantmentHelper.getAttackDamage(stack, EntityGroup.DEFAULT);
            if(damage > best) {
                index = i;
                best = damage;
            }
        }
        return index;
    }

    private int getTool(BlockPos pos) {
        int index = -1;
        double best = 0;
        for(int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.getInventory().getStack(i);
            if(stack.isEmpty()) continue;

            float speed = stack.getMiningSpeedMultiplier(MC.world.getBlockState(pos));
            if(speed <= 1) continue;

            int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if(efficiency > 0) speed += Math.pow(efficiency, 2) + 1;

            if(speed > best) {
                index = i;
                best = speed;
            }
        }
        return index;
    }
}
