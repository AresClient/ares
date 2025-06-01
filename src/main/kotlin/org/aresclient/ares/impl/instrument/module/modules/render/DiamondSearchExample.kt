package org.aresclient.ares.impl.instrument.module.modules.render

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.nrender.world.WorldDrawer
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.ChunkProcessor

object DiamondSearchExample: Module(Category.RENDER, "Diamond Search Ex", "Simple diamond search module for an example of how the chunk processor works.") {

    private var processing = false
    private val chunkProcessor = ChunkProcessor(this)
        .requireBlocks()
        .addConditions(this::findDiamonds)

    private fun findDiamonds(state: BlockState): Boolean = state.block == Blocks.DIAMOND_ORE || state.block == Blocks.DEEPSLATE_DIAMOND_ORE

    override fun onTick() {
        if(MC.NULL) return

        if(!processing) {
            chunkProcessor.begin()
            processing = true
        }
    }

    override fun onDisable() {
        chunkProcessor.end()
        processing = false
    }

    override fun onRenderWorld(matrixStack: MatrixStack, delta: Float) {
        // Blocks in the chunk processor are not thread-safe
        if(chunkProcessor.isWriteLocked) return

        val blockPos = BlockPos.Mutable()
        val offset = CAMERA.pos.negate()
        chunkProcessor.forBlock { longPos, blockState ->
            val box = Box(blockPos.set(longPos)).offset(offset)
            WorldDrawer.fillBox(box, Color.RED.deriveAlpha(0.2f))
            WorldDrawer.outlineBox(box, Color.RED, 2f)
        }
    }
}