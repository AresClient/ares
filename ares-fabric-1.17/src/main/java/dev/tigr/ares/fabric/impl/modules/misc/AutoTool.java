package dev.tigr.ares.fabric.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.core.util.Pair;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.player.DamageBlockEvent;
import dev.tigr.ares.fabric.utils.InventoryUtils;
import dev.tigr.ares.fabric.utils.WorldUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

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
                int slot = InventoryUtils.getWeapon();
                if(slot != -1 && slot != MC.player.getInventory().selectedSlot) {
                    MC.player.getInventory().selectedSlot = slot;
                    MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(MC.player.getInventory().selectedSlot));
                }
            }
        }
    });

    @EventHandler
    public EventListener<DamageBlockEvent> leftClickBlockEvent = new EventListener<>(event -> {
        int slot = InventoryUtils.getTool(event.getBlockPos());
        if(slot != -1 && slot != MC.player.getInventory().selectedSlot) {
            MC.player.getInventory().selectedSlot = slot;
            MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(MC.player.getInventory().selectedSlot));
        }
    });
}
