package dev.tigr.ares.forge.impl.modules.misc;

import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BooleanSetting;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.ares.forge.utils.InventoryUtils;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketUseEntity;
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
                int slot = InventoryUtils.getWeapon();
                if(slot != -1 && slot != MC.player.inventory.currentItem) {
                    MC.player.inventory.currentItem = slot;
                    MC.player.connection.sendPacket(new CPacketHeldItemChange());
                }
            }
        }
    });

    @EventHandler
    public EventListener<PlayerInteractEvent.LeftClickBlock> leftClickBlockEvent = new EventListener<>(event -> {
        int slot = InventoryUtils.getTool(event.getPos());
        if(slot != -1 && slot != MC.player.inventory.currentItem) {
            MC.player.inventory.currentItem = slot;
            MC.player.connection.sendPacket(new CPacketHeldItemChange());
        }
    });
}
