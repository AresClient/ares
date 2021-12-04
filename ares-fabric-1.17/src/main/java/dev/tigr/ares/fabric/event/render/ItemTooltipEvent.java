package dev.tigr.ares.fabric.event.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * @author Tigermouthbear 12/10/20
 */
public class ItemTooltipEvent {
    private final MatrixStack matrixStack;
    private final ItemStack itemStack;
    private final int x;
    private final int y;

    public ItemTooltipEvent(MatrixStack matrixStack, ItemStack itemStack, int x, int y) {
        this.matrixStack = matrixStack;
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static class Pre extends ItemTooltipEvent {
        public Pre(MatrixStack matrixStack, ItemStack itemStack, int x, int y) {
            super(matrixStack, itemStack, x, y);
        }
    }

    public static class Post extends ItemTooltipEvent {
        public Post(MatrixStack matrixStack, ItemStack itemStack, int x, int y) {
            super(matrixStack, itemStack, x, y);
        }
    }
}
