package dev.tigr.ares.forge.event.events.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;

public class StatusEffectEvent extends Event {
    private final EntityLivingBase livingEntity;
    private final Potion statusEffect;

    public StatusEffectEvent(EntityLivingBase livingEntity, Potion statusEffect) {
        this.livingEntity = livingEntity;
        this.statusEffect = statusEffect;
    }

    public EntityLivingBase getLivingEntity() {
        return livingEntity;
    }

    public Potion getStatusEffect() {
        return statusEffect;
    }
}
