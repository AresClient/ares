package org.aresclient.ares.impl.instrument.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import it.unimi.dsi.fastutil.booleans.BooleanArrayList
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.OutlineVertexConsumerProvider
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.settings.GroupedSetting
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.util.EntityUtil
import org.aresclient.ares.impl.util.EntityUtil.PlayerThreat
import org.aresclient.ares.impl.util.EntityUtil.TargetType
import org.aresclient.ares.impl.util.EntityUtil.playerThreat
import org.aresclient.ares.impl.util.RenderPipelines
import org.aresclient.ares.impl.util.RenderUtil
import java.util.*
import java.util.function.Supplier
import kotlin.collections.Set

// TODO: FIX DEPTH ON OUTLINE ESP
// TODO: MAKE THIS MORE CUSTOMIZABLE + FRIENDS
object ESP: Module(Category.RENDER, "ESP", "See outlines of entities through walls") {
    enum class Mode { OUTLINE, BOX }

    private val mode = settings.addEnum("Mode", Mode.OUTLINE)

    private class EntityGroup(color: Color, enabled: Boolean = false): MapSetting() {
        val enabled = addBoolean("Enabled", enabled).addListener { needsRegeneration = true }
        val line_color = addColor("Line Color", color).addListener { needsRegeneration = true }
        val fill_color = addColor("Fill Color", color.deriveAlpha(0.2F)).addListener { needsRegeneration = true }.setVisibility { mode.value == Mode.BOX }
    }

    private val entities = settings.addMap("Entities")

    private val playerThreatColors get() = PlayerThreat.entries.associateWith { Supplier { EntityGroup(it.defaultColor, it != PlayerThreat.BOT) } }
    private val players = entities.addGroup("Players", EntityUtil.Types.player, { EntityGroup(Color.RED, true) }, playerThreatColors)

    private val monsters = entities.addGroup("Monsters", EntityUtil.Types.monster, { EntityGroup(TargetType.HOSTILE.defaultColor) })
    private val animals = entities.addGroup("Animals", EntityUtil.Types.animal, { EntityGroup(TargetType.PASSIVE.defaultColor) })
    private val miscellaneous = entities.addGroup("Miscellaneous", EntityUtil.Types.miscellaneous, { EntityGroup(TargetType.OTHER.defaultColor) },
        mapOf(
            Pair(EntityType.ITEM, Supplier { EntityGroup(TargetType.ITEM.defaultColor) }),
            Pair(EntityType.END_CRYSTAL, Supplier { EntityGroup(TargetType.END_CRYSTAL.defaultColor, true) })
        )
    )

    private var needsRegeneration = true
    private val typeIndex = LinkedHashSet<Any>()
    private val shouldRender = BooleanArrayList()
    private val lines = ArrayList<Color>()
    private val fills = ArrayList<Color>()

    // Felt like I was losing a few fps directly checking the settings every frame
    // So I'm indexing it and only regenerating when the value changes for good measure
    private fun buildTypeIndex() {
        if (!needsRegeneration) return

        typeIndex.clear()
        shouldRender.clear()
        lines.clear()
        fills.clear()

        EntityUtil.Types.player.populateIndex(players)
        EntityUtil.Types.monster.populateIndex(monsters)
        EntityUtil.Types.animal.populateIndex(animals)
        EntityUtil.Types.miscellaneous.populateIndex(miscellaneous)

        needsRegeneration = false
    }

    private fun <T: Any> Set<T>.populateIndex(group: GroupedSetting<T, EntityGroup>) {
        for (type in this) {
            val map = group.getValue(type)
            typeIndex.add(type)
            shouldRender.add(map.enabled.value)
            lines.add(map.line_color.value)
            fills.add(map.fill_color.value)
        }
    }

    private val Entity.index get() =
        if (type == EntityType.PLAYER) typeIndex.indexOf((this as PlayerEntity).playerThreat)
        else typeIndex.indexOf(type)

    fun getEntityColor(entity: Entity): Color = lines[entity.index]

    fun shouldRenderOutline() = isEnabled() && mode.value == Mode.OUTLINE

    @JvmStatic fun shouldRenderOutline(entity: Entity) =
        shouldRenderOutline() && entity.index != -1 && shouldRender.getBoolean(entity.index)

    override fun onRenderWorld(delta: Float, renderer: Renderer.State) {
        buildTypeIndex()
        if(mode.value != Mode.BOX) return
        WORLD.entities?.forEach { entity ->
            if (entity == SELF) return@forEach

            val i = entity.index
            if (!shouldRender.getBoolean(i)) return@forEach
            val box = entity.getInterpolatedBoundingBox(delta)
            RenderUtil.Lines.box(box, lines[i], 2F)
            RenderUtil.Fill.box(box, fills[i])
        }
    }

    private fun Entity.getInterpolatedBoundingBox(delta: Float): Box {
        val camera = MC.gameRenderer.camera.pos
        val x = MathHelper.lerp(delta.toDouble(), lastRenderX, x) - x - camera.x
        val y = MathHelper.lerp(delta.toDouble(), lastRenderY, y) - y - camera.y
        val z = MathHelper.lerp(delta.toDouble(), lastRenderZ, z) - z - camera.z
        return Box(
            boundingBox.minX + x, boundingBox.minY + y, boundingBox.minZ + z,
            boundingBox.maxX + x, boundingBox.maxY + y, boundingBox.maxZ + z
        )
    }

    // see MixinWorldRenderer.java
    object Outliner {
        val framebuffer = SimpleFramebuffer("Ares Outline Framebuffer", MC.framebuffer.textureWidth, MC.framebuffer.textureHeight, true)
        val vertexConsumerProvider = OutlineVertexConsumerProvider(MC.bufferBuilders.entityVertexConsumers)

        fun setColor(color: Color) {
            vertexConsumerProvider.setColor((color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt(), 255) // TODO: ALPHA??
        }

        fun blit() {
            RenderSystem.assertOnRenderThread()
            val shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS)
            val gpuBuffer = shapeIndexBuffer.getIndexBuffer(6)
            val gpuBuffer2 = RenderSystem.getQuadVertexBuffer()
            RenderSystem.getDevice().createCommandEncoder().createRenderPass(MC.framebuffer.colorAttachment, OptionalInt.empty())
            .use { renderPass ->
                renderPass.setPipeline(RenderPipelines.outline)
                renderPass.setVertexBuffer(0, gpuBuffer2)
                renderPass.setIndexBuffer(gpuBuffer, shapeIndexBuffer.indexType)
                renderPass.bindSampler("theTexture", framebuffer.colorAttachment)
                renderPass.setUniform("viewportSize", MC.framebuffer.textureWidth.toFloat(), MC.framebuffer.textureHeight.toFloat())
                renderPass.setUniform("lineWeight", 1f)
                renderPass.drawIndexed(0, 6)
            }
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(framebuffer.colorAttachment, 0, framebuffer.depthAttachment, 0.0)
        }
    }
}
