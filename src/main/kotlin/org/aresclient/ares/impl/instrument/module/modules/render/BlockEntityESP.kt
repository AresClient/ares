package org.aresclient.ares.impl.instrument.module.modules.render

import net.minecraft.block.ChestBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.enums.ChestType
import net.minecraft.client.render.Frustum
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.api.setting.settings.grouped.Group
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.ChunkProcessor
import org.aresclient.ares.impl.util.RenderUtil
import org.aresclient.ares.impl.util.WorldUtil
import org.aresclient.ares.impl.util.WorldUtil.boundingBox
import org.aresclient.ares.mixin.accessors.AccessWorldRenderer
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
        val lump = addBoolean("Lump", false)
    }

    private val blockEntities = settings.addGrouped("BlockEntities", arrayListOf(
        BlockEntityGroup.create("Chests", setOf(BlockEntityType.CHEST, BlockEntityType.TRAPPED_CHEST, BlockEntityType.BARREL), Color(0.87f, 0.65f, 0.25f, 1f)),
        BlockEntityGroup.create("Ender Chests", setOf(BlockEntityType.ENDER_CHEST), Color(0.7f, 0f, 0.7f, 1f)),
        BlockEntityGroup.create("Shulker Boxes", setOf(BlockEntityType.SHULKER_BOX), Color(1f, 0.45f, 0.55f, 1f)),
        BlockEntityGroup.create("Other Storage", setOf(BlockEntityType.FURNACE, BlockEntityType.BLAST_FURNACE, BlockEntityType.DISPENSER, BlockEntityType.DROPPER,
                                                            BlockEntityType.HOPPER, BlockEntityType.SMOKER), Color(0.65f, 0.65f, 0.65f, 1f))
    ), WorldUtil.BlockEntityTypes.possibles, { BlockEntityGroup() })

    private val blockEntitiesCache = hashMapOf<BlockEntityType<*>, BlockEntityGroup?>()

    private val chunkProcessor = ChunkProcessor(this).requireBlockEntities()

    override fun onEnable() {
        chunkProcessor.begin()
    }

    override fun onDisable() {
        chunkProcessor.end()
    }

    override fun onTick() {
        if(MC.NULL) return
        blockEntitiesCache.clear()
    }

    private fun getBlockEntityGroup(blockEntity: BlockEntity): BlockEntityGroup? {
        return blockEntitiesCache.getOrPut(blockEntity.type) { blockEntities.find(blockEntity.type).getOrNull() }
    }

    override fun onRenderWorld3d(delta: Float, renderer: Renderer.State) {
        if(blockEntities.none { it.enabled.value }) return

        val offset = CAMERA.pos.negate()
        val frustum = Frustum(MC.worldRenderer.capturedFrustum ?: (MC.worldRenderer as AccessWorldRenderer).frustum)
        chunkProcessor.getBlockEntities().forEach { blockEntity ->
            val group = getBlockEntityGroup(blockEntity) ?: return@forEach
            if(!group.enabled.value || blockEntity.shouldCull(frustum)) return@forEach

            var box: Box?
            val ignoreDirections = arrayOfNulls<Direction>(6)

            // TODO: Lump with greedy meshing?
            if(group.lump.value) {
                box = Box(blockEntity.pos).offset(offset)
                for(direction in Direction.entries) {
                    ignoreDirections[direction.index] = null
                    if(blockEntity.pos.offset(direction).shouldLump(group)) {
                        ignoreDirections[direction.index] = direction
                    }
                }
            } else box = blockEntity.pos.boundingBox?.offset(offset) ?: Box(blockEntity.pos).offset(offset)

            if(blockEntity.type == BlockEntityType.CHEST && !group.lump.value && blockEntity.cachedState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                val stretch = ChestBlock.getFacing(blockEntity.cachedState)
                box = box!!.doubleChest(stretch)
            }

            box ?: return@forEach

            RenderUtil.Lines.box(box, group.lineColor.value, 2f, *ignoreDirections)
            RenderUtil.Fill.box(box, group.fillColor.value, *ignoreDirections)
        }
    }

    private fun BlockEntity.shouldCull(frustum: Frustum) = pos.boundingBox?.let { !frustum.isVisible(it.expand(0.5)) } ?: true

    private fun BlockPos.shouldLump(group: BlockEntityGroup): Boolean {
        val blockEntity = WORLD.getBlockEntity(this) ?: return false
        val group2 = getBlockEntityGroup(blockEntity) ?: return false
        return group == group2
    }

    private fun Box.doubleChest(face: Direction): Box? = when (face) {
        Direction.EAST -> Box(minX, minY, minZ, maxX + 0.94, maxY, maxZ)
        Direction.SOUTH -> Box(minX, minY, minZ, maxX, maxY, maxZ + 0.94)
        else -> null
    }
}
