package org.aresclient.ares.impl.util

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket
import net.minecraft.registry.RegistryKey
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.world.World
import net.minecraft.world.chunk.ChunkSection
import net.minecraft.world.chunk.WorldChunk
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.events.*
import org.aresclient.ares.api.instruments.Component
import org.aresclient.ares.api.instruments.Instrument

class ChunkProcessor<I: Instrument>(master: I): Component<I>(master), Wrapper {
    private val chunks = mutableListOf<WorldChunk>()
    private val blockEntities = mutableListOf<BlockEntity>()
    private val blocks = Long2ObjectOpenHashMap<BlockState>()
    private val conditions = mutableListOf<(BlockState) -> Boolean>()
    private var dimension: RegistryKey<World>? = null

    fun getChunks(): List<WorldChunk> = ArrayList(chunks)
    fun getBlockEntities(): List<BlockEntity> = ArrayList(blockEntities)
    fun getBlocks(): Long2ObjectOpenHashMap<BlockState> = blocks.clone()

    private var requiresBlockEntities = false
    fun requireBlockEntities(): ChunkProcessor<I> {
        requiresBlockEntities = true
        return this
    }

    private var requiresBlocks = false
    fun requireBlocks(): ChunkProcessor<I> {
        requiresBlocks = true
        return this
    }

    fun begin(): ChunkProcessor<I> {
        chunks.addAll(getLoadedChunks())
        dimension = WorldUtil.getDimension()
        if (requiresBlockEntities) chunks.forEach { blockEntities.addAll(it.blockEntities.values) }
        if (requiresBlocks) populateBlockMap()
        return this
    }

    fun end() {
        chunks.clear()
        if (requiresBlockEntities) blockEntities.clear()
        if (requiresBlocks) blocks.clear()
    }

    /** Add conditions that should be met for a block to be added to the list */
    fun addConditions(vararg conditions: (BlockState) -> Boolean): ChunkProcessor<I> {
        this.conditions.addAll(conditions)
        blocks.clear()
        populateBlockMap()
        return this
    }

    /** Remove conditions for blocks that are added to the list */
    fun removeConditions(vararg conditions: (BlockState) -> Boolean): ChunkProcessor<I> {
        this.conditions.removeAll(conditions.toSet())
        blocks.clear()
        populateBlockMap()
        return this
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
        if (requiresBlocks) searchChunk(event.chunk)
    }

    @field:EventHandler private val onBlockEntityEvent = EventListener<BlockEntityEvent> { event ->
        if (!requiresBlockEntities) return@EventListener
        when (event) {
            is BlockEntityEvent.Add -> blockEntities.add(event.blockEntity ?: return@EventListener)
            is BlockEntityEvent.Remove -> blockEntities.remove(event.blockEntity)
        }
    }

    @field:EventHandler private val onPacketReceived = EventListener<PacketEvent.Receive> { event ->
        if (event.era != Era.BEFORE) return@EventListener

        val packet = event.packet
        when (packet) {
            is UnloadChunkS2CPacket -> WORLD.chunkManager.getWorldChunk(packet.pos.x, packet.pos.z)?.let {
                chunks.remove(it)
                if (requiresBlockEntities) blockEntities.removeAll(it.blockEntities.values)
                if (requiresBlocks) removeChunkBlocks(it)
            }
        }
    }

    @field:EventHandler private val onBlockUpdate = EventListener<BlockStateUpdateEvent> { event ->
        if (!requiresBlocks) return@EventListener

        for (condition in conditions) { // Remove old state
            event.oldState ?: break

            if (!condition.invoke(event.oldState)) continue

            blocks.remove(event.pos.asLong())
            break
        }

        for (condition in conditions) { // Add new state
            event.newState ?: break

            if (!condition.invoke(event.newState)) continue

            blocks.put(event.pos.asLong(), event.newState)
            break
        }
    }

    private fun getLoadedChunks(): List<WorldChunk> {
        val chunks = mutableListOf<WorldChunk>()

        if (MC.NULL) return chunks

        val chunkPos = SELF.chunkPos
        val viewDistance = MC.options.viewDistance.value
        for(x in -viewDistance..viewDistance) for(z in -viewDistance..viewDistance) {
            WORLD.chunkManager.getWorldChunk(chunkPos.x + x, chunkPos.z + z)?.let {
                chunks.add(it)
            }
        }

        return chunks
    }

    private fun populateBlockMap() {
        if (!requiresBlocks) return
        EXECUTOR.execute {
            for (i in 0..chunks.lastIndex) searchChunk(chunks[i])
        }
    }

    private fun searchChunk(chunk: WorldChunk) {
        if (!requiresBlocks) return
        val sections = chunk.sectionArray
        for (i in 0..sections.lastIndex) {
            val section = sections[i] ?: continue
            searchSection(section, ChunkSectionPos.from(chunk.pos, chunk.bottomSectionCoord + i))
        }
    }

    var pauseForBlockMapUpdate = false
        private set

    private fun searchSection(section: ChunkSection, sectionPos: ChunkSectionPos) {
        pauseForBlockMapUpdate = true
        // More efficient to iterate through section local positions rather than global block positions
        for (y in 0..15) for (z in 0..15) for (x in 0..15) {
            val state = section.getBlockState(x, y, z)
            for (condition in conditions) if (condition.invoke(state)) {
                blocks.put(BlockPos.asLong(sectionPos.minX + x, sectionPos.minY + y, sectionPos.minZ + z), state)
            }
        }
        pauseForBlockMapUpdate = false
    }

    private fun removeChunkBlocks(chunk: WorldChunk) {
        pauseForBlockMapUpdate = true
        val copy = blocks.clone()
        var chunkX: Int
        var chunkZ: Int
        for (longPos in copy.keys) {
            chunkX = BlockPos.unpackLongX(longPos) shr 4
            chunkZ = BlockPos.unpackLongZ(longPos) shr 4
            if (chunk.pos.x == chunkX && chunk.pos.z == chunkZ) {
                copy.remove(longPos)
            }
        }
        blocks.clear()
        blocks.putAll(copy)
        pauseForBlockMapUpdate = false
    }
}