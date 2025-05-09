package org.aresclient.ares.impl.util

import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.registry.Registries
import net.minecraft.util.Language
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.setting.settings.grouped.GroupMember
import org.aresclient.ares.api.setting.settings.grouped.GroupMembers
import org.aresclient.ares.api.setting.settings.grouped.IGroupMember

object WorldUtil: Wrapper {
    object BlockEntityTypes {
        val storage = HashSet<BlockEntityType<*>>()
        val miscellaneous = HashSet<BlockEntityType<*>>()

        val possibles: Set<IGroupMember<BlockEntityType<*>>>

        init {
            Registries.BLOCK_ENTITY_TYPE.forEach {
                when(it) {
                    BlockEntityType.CHEST, BlockEntityType.BARREL, BlockEntityType.ENDER_CHEST, BlockEntityType.SHULKER_BOX,
                    BlockEntityType.TRAPPED_CHEST, BlockEntityType.FURNACE, BlockEntityType.BLAST_FURNACE, BlockEntityType.DISPENSER,
                    BlockEntityType.DROPPER, BlockEntityType.HOPPER, BlockEntityType.SMOKER
                        -> storage.add(it)
                    else -> miscellaneous.add(it)
                }
            }

            possibles = setOf(
                GroupMembers("Storage", storage.toGroupMembers()),
                GroupMembers("Miscellaneous", miscellaneous.toGroupMembers())
            )
        }

        private fun HashSet<BlockEntityType<*>>.toGroupMembers() = map { type ->
            val id = BlockEntityType.getId(type).toString()
            GroupMember(id, Language.getInstance().get(id), type)
        }
    }

    fun getBlockEntities(): List<BlockEntity> {
        val blockEntities = mutableListOf<BlockEntity>()
        val chunkPos = MC.player!!.chunkPos
        val viewDistance = MC.options.viewDistance.value
        for(x in -viewDistance..viewDistance) {
            for(z in -viewDistance..viewDistance) {
                MC.world!!.chunkManager.getWorldChunk(chunkPos.x + x, chunkPos.z + z)?.let {
                    blockEntities.addAll(it.blockEntities.values)
                }
            }
        }

        return blockEntities
    }
}
