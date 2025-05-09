package org.aresclient.ares.impl.instrument.module.modules.render

import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.api.setting.settings.grouped.Group
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.RenderUtil
import org.aresclient.ares.impl.util.WorldUtil
import java.util.HashSet
import kotlin.jvm.optionals.getOrNull

object BlockEntityESP: Module(Category.RENDER, "BlockEntityESP", "See outlines of block entities through walls") {
    private class BlockEntityGroup(members: Set<BlockEntityType<*>> = emptySet()): Group<BlockEntityType<*>>(members) {
        companion object {
            fun create(title: String, members: Collection<BlockEntityType<*>>, color: Color, enabled: Boolean = true): BlockEntityGroup {
                return BlockEntityGroup(HashSet(members)).also {
                    it.title.value = title
                    it.lineColor.value = color
                    it.fillColor.value = color.deriveAlpha(0.2f)
                    it.enabled.value = enabled
                }
            }
        }

        val lineColor: ColorSetting = addColor("Line Color", Color.WHITE)
        val fillColor: ColorSetting = addColor("Fill Color", Color.WHITE.deriveAlpha(0.2f))
    }

    private val blockEntities = settings.addGrouped("BlockEntities", arrayListOf(
        BlockEntityGroup.create("Chests", setOf(BlockEntityType.CHEST, BlockEntityType.TRAPPED_CHEST, BlockEntityType.BARREL), Color(0f, 0f, 0.89f, 1f)),
        BlockEntityGroup.create("Ender Chests", setOf(BlockEntityType.ENDER_CHEST), Color(0.7f, 0f, 0.7f, 1f)),
        BlockEntityGroup.create("Shulker Boxes", setOf(BlockEntityType.SHULKER_BOX), Color(1f, 0.45f, 0.55f, 1f)),
        BlockEntityGroup.create("Other Storage", setOf(BlockEntityType.FURNACE, BlockEntityType.BLAST_FURNACE, BlockEntityType.DISPENSER, BlockEntityType.DROPPER,
                                                            BlockEntityType.HOPPER, BlockEntityType.SMOKER), Color(0.65f, 0.65f, 0.65f, 1f))
    ), WorldUtil.BlockEntityTypes.possibles, { BlockEntityGroup() })

    private val blockEntitiesCache = hashMapOf<BlockEntityType<*>, BlockEntityGroup?>()

    override fun onTick() {
        blockEntitiesCache.clear()
    }

    private fun getBlockEntityGroup(blockEntity: BlockEntity): BlockEntityGroup? {
        return blockEntitiesCache.getOrPut(blockEntity.type) { blockEntities.find(blockEntity.type).getOrNull() }
    }

    override fun onRenderWorld3d(delta: Float, renderer: Renderer.State) {
        if(blockEntities.none { it.enabled.value }) return

        val offset = MC.gameRenderer.camera.pos.negate()
        WorldUtil.getBlockEntities().forEach { blockEntity ->
            val group = getBlockEntityGroup(blockEntity) ?: return@forEach
            if(!group.enabled.value) return@forEach

            val box = blockEntity.cachedState.getOutlineShape(MC.world, blockEntity.pos).boundingBox.offset(blockEntity.pos).offset(offset)
            RenderUtil.Lines.box(box, group.lineColor.value, 2f)
            RenderUtil.Fill.box(box, group.fillColor.value)
        }
    }
}
