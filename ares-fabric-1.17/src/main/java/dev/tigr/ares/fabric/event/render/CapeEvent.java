package dev.tigr.ares.fabric.event.render;

import dev.tigr.ares.core.util.render.Color;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

public class CapeEvent {
    private final AbstractClientPlayerEntity abstractClientPlayerEntity;
    private Identifier identifier = null;
    private Color color = null;

    public CapeEvent(AbstractClientPlayerEntity abstractClientPlayerEntity) {
        this.abstractClientPlayerEntity = abstractClientPlayerEntity;
    }

    public AbstractClientPlayerEntity getAbstractClientPlayerEntity() {
        return abstractClientPlayerEntity;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
