package org.aresclient.ares.impl.instrument.module.modules.render

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.ChunkProcessor
import org.aresclient.ares.impl.util.RenderUtil

object DiamondSearchExample: Module(Category.RENDER, "Diamond Search Ex", "Simple diamond search module for an example of how the chunk processor works.") {

    private val chunkProcessor = ChunkProcessor(this)
        .requireBlocks()
        .addConditions(this::findDiamonds)

    private fun findDiamonds(state: BlockState): Boolean = state.block == Blocks.DIAMOND_ORE || state.block == Blocks.DEEPSLATE_DIAMOND_ORE

    override fun onEnable() {
        chunkProcessor.begin()
    }

    override fun onDisable() {
        chunkProcessor.end()
    }

    override fun onRenderWorld3d(delta: Float, renderer: Renderer.State) {
        // Blocks in the chunk processor are not thread-safe
        if(chunkProcessor.isWriteLocked) return

        val blockPos = BlockPos.Mutable()
        val offset = CAMERA.pos.negate()
        chunkProcessor.forBlock { longPos, blockState ->
            val box = Box(blockPos.set(longPos)).offset(offset)
            RenderUtil.Fill.box(box, Color.RED.deriveAlpha(0.2f))
            RenderUtil.Lines.box(box, Color.RED, 2f)
        }
    }
}