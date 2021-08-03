package dev.tigr.ares.forge.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.global.ReflectionHelper;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoTool", description = "Automatically picks the best tool for the job", category = Category.MISC)
public class AutoTool extends Module {
    private final Setting<Boolean> endCrystals = register(new BooleanSetting("End Crystals", false));

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof CPacketUseEntity) {
            if(((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK) {
                if(((CPacketUseEntity) event.getPacket()).getEntityFromWorld(MC.world) instanceof EntityEnderCrystal && !endCrystals.getValue())
                    return;
                int slot = getWeapon();
                if(slot != -1 && slot != MC.player.inventory.currentItem) {
                    MC.player.inventory.currentItem = slot;
                    MC.player.connection.sendPacket(new CPacketHeldItemChange());
                }
            }
        }
    });

    @EventHandler
    public EventListener<PlayerInteractEvent.LeftClickBlock> leftClickBlockEvent = new EventListener<>(event -> {
        int slot = getTool(event.getPos());
        if(slot != -1 && slot != MC.player.inventory.currentItem) {
            MC.player.inventory.currentItem = slot;
            MC.player.connection.sendPacket(new CPacketHeldItemChange());
        }
    });

    private int getWeapon() {
        int index = -1;
        double best = 0;
        for(int i = 0; i < 9; i++) {
            ItemStack stack = MC.player.inventory.getStackInSlot(i);
            if(stack.isEmpty()) continue;
            double damage = -1;
            Item item = stack.getItem();
            if(item instanceof ItemTool)
                damage = (float) (ReflectionHelper.getPrivateValue(ItemTool.class, (ItemTool) item, "attackDamage", "field_77865_bY")) + (double) EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
            if(item instanceof ItemSword)
                damage = ((ItemSword) item).getAttackDamage() + (double) EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
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
            ItemStack stack = MC.player.inventory.getStackInSlot(i);
            if(stack.isEmpty()) continue;

            float speed = stack.getDestroySpeed(MC.world.getBlockState(pos));
            if(speed <= 1) continue;

            int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
            if(efficiency > 0) speed += Math.pow(efficiency, 2) + 1;

            if(speed > best) {
                index = i;
                best = speed;
            }
        }
        return index;
    }
}
