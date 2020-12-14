package dev.tigr.ares.fabric.event.player;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;

public class StatusEffectEvent extends Event {
    private final LivingEntity livingEntity;
    private final StatusEffect statusEffect;

    public StatusEffectEvent(LivingEntity livingEntity, StatusEffect statusEffect) {
        this.livingEntity = livingEntity;
        this.statusEffect = statusEffect;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }
}
