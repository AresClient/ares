package dev.tigr.ares.fabric.impl.modules.combat;

import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.EnumSetting;
import dev.tigr.ares.core.setting.settings.StringSetting;
import dev.tigr.ares.fabric.event.client.LivingDeathEvent;
import dev.tigr.ares.fabric.event.client.PacketEvent;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

/**
 * @author Tigermouthbear
 */
@Module.Info(name = "AutoEz", description = "Automatically ez people in chat", category = Category.COMBAT, alwaysListening = true)
public class AutoEz extends Module {
    private final Setting<Messages> mode = register(new EnumSetting<>("Message", Messages.ARES_OWNS_ME));
    private final Setting<String> custom = register(new StringSetting("Custom", "ez")).setVisibility(() -> mode.getValue() == Messages.CUSTOM);
    private int hasBeenCombat;
    private PlayerEntity target;

    @EventHandler
    public EventListener<PacketEvent.Sent> packetSentEvent = new EventListener<>(event -> {
        if(event.getPacket() instanceof PlayerInteractEntityC2SPacket) {
            PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket) event.getPacket();

            if(packet.getType() == PlayerInteractEntityC2SPacket.InteractionType.ATTACK) {
                Entity e = packet.getEntity(MC.world);
                if(e instanceof PlayerEntity) {
                    target = (PlayerEntity) e;
                    hasBeenCombat = 500;
                }

                if(e instanceof EndCrystalEntity) {
                    PlayerEntity newTarget = null;
                    for(PlayerEntity entityPlayer: MC.world.getPlayers()) {
                        if(FriendManager.isFriend(entityPlayer.getGameProfile().getName()) || entityPlayer.isDead()) continue;
                        if((newTarget == null && entityPlayer.distanceTo(e) < 4) ||
                                (newTarget != null && MC.player.distanceTo(entityPlayer) < MC.player.distanceTo(newTarget)))
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
        if(event.getEntity() instanceof PlayerEntity) {
            if(getEnabled() && hasBeenCombat > 0 && (target.getHealth() <= 0 || target.isDead() || !MC.world.getPlayers().contains(target)))
                MC.player.sendChatMessage(mode.getValue() == Messages.CUSTOM ? custom.getValue() : mode.getValue().name);

            hasBeenCombat = 0;
        }
    });

    private int sinceLastMessage = 0;

    @Override
    public void onTick() {
        if(MC.player.isDead()) hasBeenCombat = 0;

        if(sinceLastMessage == 0 && hasBeenCombat > 0 && (target.getHealth() <= 0 || target.isDead())) {
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
