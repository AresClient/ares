package dev.tigr.ares.fabric.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.ares.fabric.event.player.DamageBlockEvent;
import dev.tigr.ares.fabric.utils.InventoryUtils;
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
            if(((PlayerInteractEntityC2SPacket) event.getPacket()).getType() == PlayerInteractEntityC2SPacket.InteractionType.ATTACK) {
                if(((PlayerInteractEntityC2SPacket) event.getPacket()).getEntity(MC.world) instanceof EndCrystalEntity && !endCrystals.getValue())
                    return;
                int slot = InventoryUtils.getWeapon();
                if(slot != -1 && slot != MC.player.inventory.selectedSlot) {
                    MC.player.inventory.selectedSlot = slot;
                    MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket());
                }
            }
        }
    });

    @EventHandler
    public EventListener<DamageBlockEvent> leftClickBlockEvent = new EventListener<>(event -> {
        int slot = InventoryUtils.getTool(event.getBlockPos());
        if(slot != -1 && slot != MC.player.inventory.selectedSlot) {
            MC.player.inventory.selectedSlot = slot;
            MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket());
        }
    });
}
