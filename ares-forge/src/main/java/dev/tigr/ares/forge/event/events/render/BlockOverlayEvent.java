package dev.tigr.ares.forge.event.events.render;

import dev.tigr.simpleevents.event.Event;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;

public class BlockOverlayEvent extends Event {
    private final EntityPlayer player;
    private final float renderPartialTicks;
    private final RenderBlockOverlayEvent.OverlayType overlayType;
    private final IBlockState blockForOverlay;
    private final BlockPos blockPos;

    @Deprecated
    public BlockOverlayEvent(EntityPlayer player, float renderPartialTicks, RenderBlockOverlayEvent.OverlayType type, Block block, int x, int y, int z) {
        this(player, renderPartialTicks, type, block.getDefaultState(), new BlockPos(x, y, z));
    }

    public BlockOverlayEvent(EntityPlayer player, float renderPartialTicks, RenderBlockOverlayEvent.OverlayType type, IBlockState block, BlockPos blockPos) {
        this.player = player;
        this.renderPartialTicks = renderPartialTicks;
        this.overlayType = type;
        this.blockForOverlay = block;
        this.blockPos = blockPos;

    }

    /**
     * The player which the overlay will apply to
     */
    public EntityPlayer getPlayer() {
        return player;
    }

    public float getRenderPartialTicks() {
        return renderPartialTicks;
    }

    /**
     * The type of overlay to occur
     */
    public RenderBlockOverlayEvent.OverlayType getOverlayType() {
        return overlayType;
    }

    /**
     * If the overlay type is BLOCK, then this is the block which the overlay is getting it's icon from
     */
    public IBlockState getBlockForOverlay() {
        return blockForOverlay;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public enum OverlayType {
        FIRE, BLOCK, WATER
    }
}
