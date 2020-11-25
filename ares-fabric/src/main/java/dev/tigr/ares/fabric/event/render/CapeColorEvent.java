package dev.tigr.ares.fabric.event.render;

import dev.tigr.ares.core.util.render.Color;
import net.minecraft.entity.player.PlayerEntity;

public class CapeColorEvent {
    private final PlayerEntity playerEntity;
    private Color color = null;

    public CapeColorEvent(PlayerEntity playerEntity) {
        this.playerEntity = playerEntity;
    }

    public PlayerEntity getPlayerEntity() {
        return playerEntity;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
