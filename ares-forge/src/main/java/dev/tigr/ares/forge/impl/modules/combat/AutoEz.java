package dev.tigr.ares.forge.impl.modules.combat;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.StringSetting;
import dev.tigr.ares.forge.event.events.player.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoEz", description = "Automatically ez people in chat", category = Category.COMBAT, alwaysListening = true)
public class AutoEz extends Module {
    private final Setting<Messages> mode = register(new EnumSetting<>("Message", Messages.ARES_OWNS_ME));
    private final Setting<String> custom = register(new StringSetting("Custom", "ez")).setVisibility(() -> mode.getValue() == Messages.CUSTOM);
    private int hasBeenCombat;
    private EntityPlayer target;

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();

            if(packet.getAction() == CPacketUseEntity.Action.ATTACK) {
                Entity e = packet.getEntityFromWorld(MC.world);
                if(e instanceof EntityPlayer) {
                    target = (EntityPlayer) e;
                    hasBeenCombat = 500;
                }

                if(e instanceof EntityEnderCrystal) {
                    EntityPlayer newTarget = null;
                    for(EntityPlayer entityPlayer: MC.world.playerEntities) {
                        if(FriendManager.isFriend(entityPlayer.getGameProfile().getName()) || entityPlayer.isDead) continue;
                        if((newTarget == null && entityPlayer.getDistance(e) < 4) ||
                                (newTarget != null && MC.player.getDistanceSq(entityPlayer) < MC.player.getDistanceSq(newTarget)))
                            newTarget = entityPlayer;
                    }

                    if(newTarget != null) {
                        target = newTarget;
                        hasBeenCombat = 40;
                    }
                }
            }
        }
    });
    @EventHandler
    public EventListener<LivingDeathEvent> deathEvent = new EventListener<>(event -> {
        if(event.getEntity() instanceof EntityPlayer) {
            if(getEnabled() && hasBeenCombat > 0 && (target.getHealth() <= 0 || target.isDead || !MC.world.playerEntities.contains(target)))
                MC.player.sendChatMessage(mode.getValue() == Messages.CUSTOM ? custom.getValue() : mode.getValue().name);

            hasBeenCombat = 0;
        }
    });
    private int sinceLastMessage = 0;

    @Override
    public void onTick() {
        if(MC.player.isDead) hasBeenCombat = 0;

        if(sinceLastMessage == 0 && hasBeenCombat > 0 && (target.getHealth() <= 0 || target.isDead)) {
            if(getEnabled())
                MC.player.sendChatMessage(mode.getValue() == Messages.CUSTOM ? custom.getValue() : mode.getValue().name);
            sinceLastMessage = 80;
            hasBeenCombat = 0;
        }

        if(sinceLastMessage > 0) sinceLastMessage--;

        hasBeenCombat--;
    }

    enum Messages {
        ARES_WEBSITE("Nice fight, get an actual client at aresclient.org"),
        ARES_ON_TOP("Get rekt, Ares on top"),
        EZ("ez"),
        TIGERMOUTHBEAR("Nice fight, tigermouthbear owns me and all!"),
        ARES_OWNS_ME("Nice fight, Ares Client owns me and all!"),
        CUSTOM("");

        private final String name;

        Messages(String name) {
            this.name = name;
        }
    }
}
