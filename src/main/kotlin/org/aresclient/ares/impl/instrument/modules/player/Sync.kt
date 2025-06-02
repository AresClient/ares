package org.aresclient.ares.impl.instrument.modules.player

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.minecraft.util.math.BlockPos
import org.aresclient.ares.api.events.BlockStateUpdateEvent
import org.aresclient.ares.api.instruments.Module

object Sync: Module(Category.PLAYER, "Sync", "Attempts to prevent desync between the client and the server.") {
    private val ghost_blocks = settings.addBoolean("Ghost Blocks", true, "Prevent ghost blocks caused by breaking too quickly.")
    private val false_blocks = settings.addBoolean("False Blocks", true, "Prevent false blocks created when placing that do not exist on the server.")

    /** @see org.aresclient.ares.mixin.mixins.MixinClientPlayerInteractionManager.onBreakBlockSetBlockState */
    val ghostBlocks: Boolean get() = this.isListening() && ghost_blocks.value

    /**
     * @see org.aresclient.ares.mixin.mixins.MixinBlockItem.onPlace
     * @see org.aresclient.ares.mixin.mixins.MixinBlockItem.onPlaceIsBlockStateOf
     */
    val falseBlocks: Boolean get() = this.isListening() && false_blocks.value

    /**
     * @see org.aresclient.ares.mixin.mixins.MixinClientPlayerInteractionManager.onAttackBlock
     * @see org.aresclient.ares.mixin.mixins.MixinClientPlayerInteractionManager.onUpdateBlockBreakingProgress
     */
    val removedBlocksSet = mutableSetOf<BlockPos>()

    @field:EventHandler val onBlockStateUpdate = EventListener<BlockStateUpdateEvent> { event ->
        removedBlocksSet.remove(event.pos)
    }

    override fun onDisable() {
        removedBlocksSet.clear()
    }
}