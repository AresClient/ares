package org.aresclient.ares.impl.util

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.minecraft.block.entity.BlockEntity
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket
import net.minecraft.registry.RegistryKey
import net.minecraft.world.World
import net.minecraft.world.chunk.WorldChunk
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.events.*
import org.aresclient.ares.api.instruments.Component
import org.aresclient.ares.api.instruments.Instrument

// TODO: (Toggleable) Block Processing for Search
class ChunkProcessor<I: Instrument>(master: I): Component<I>(master), Wrapper {
    private val chunks = mutableListOf<WorldChunk>()
    private val blockEntities = mutableListOf<BlockEntity>()
    private var dimension: RegistryKey<World>? = null

    fun getChunks(): List<WorldChunk> = ArrayList(chunks)
    fun getBlockEntities(): List<BlockEntity> = ArrayList(blockEntities)

    fun begin(): ChunkProcessor<I> {
        chunks.addAll(getLoadedChunks())

        chunks.forEach {
            blockEntities.addAll(it.blockEntities.values)
        }

        dimension = WorldUtil.getDimension()

        return this
    }

    fun end() {
        chunks.clear()
        blockEntities.clear()
    }

    @field:EventHandler private val onTick = EventListener<TickEvent.Client> { event ->
        if (event.era != Era.AFTER || MC.NULL) return@EventListener

        val currentDimension = WorldUtil.getDimension() ?: return@EventListener
        if (dimension == currentDimension) return@EventListener

        end()
        begin()
    }

    @field:EventHandler private val onLoadChunk = EventListener<LoadChunkEvent> { event ->
        chunks.add(event.chunk)
    }

    @field:EventHandler private val onBlockEntityEvent = EventListener<BlockEntityEvent> { event ->
        when (event) {
            is BlockEntityEvent.Add -> blockEntities.add(event.blockEntity ?: return@EventListener)
            is BlockEntityEvent.Remove -> blockEntities.remove(event.blockEntity)
        }
    }

    @field:EventHandler private val onPacketReceived = EventListener<PacketEvent.Receive> { event ->
        if (event.era != Era.AFTER) return@EventListener

        val packet = event.packet
        when (packet) {
            is UnloadChunkS2CPacket -> WORLD.chunkManager.getWorldChunk(packet.pos.x, packet.pos.z)?.let {
                chunks.remove(it)
                blockEntities.removeAll(it.blockEntities.values)
            }
        }
    }

    private fun getLoadedChunks(): List<WorldChunk> {
        val chunks = mutableListOf<WorldChunk>()

        if (MC.NULL) return chunks

        val chunkPos = SELF.chunkPos
        val viewDistance = MC.options.viewDistance.value
        for(x in -viewDistance..viewDistance) {
            for(z in -viewDistance..viewDistance) {
                WORLD.chunkManager.getWorldChunk(chunkPos.x + x, chunkPos.z + z)?.let {
                    chunks.add(it)
                }
            }
        }

        return chunks
    }
}