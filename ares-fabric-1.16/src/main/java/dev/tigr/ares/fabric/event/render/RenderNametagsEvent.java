package dev.tigr.ares.fabric.event.render;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

public class RenderNametagsEvent extends Event {
    private final MatrixStack matrixStack;
    private final AbstractClientPlayerEntity playerEntity;

    public RenderNametagsEvent(MatrixStack matrixStack, AbstractClientPlayerEntity abstractClientPlayerEntity) {
        this.matrixStack = matrixStack;
        this.playerEntity = abstractClientPlayerEntity;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public AbstractClientPlayerEntity getPlayerEntity() {
        return playerEntity;
    }
}