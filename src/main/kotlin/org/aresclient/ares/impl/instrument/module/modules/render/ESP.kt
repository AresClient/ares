package org.aresclient.ares.impl.instrument.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.OutlineVertexConsumerProvider
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.api.setting.settings.EnumSetting
import org.aresclient.ares.api.setting.settings.grouped.Group
import org.aresclient.ares.api.setting.settings.grouped.GroupMember
import org.aresclient.ares.api.setting.settings.grouped.GroupMembers
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.api.util.StringUtils.formatToPretty
import org.aresclient.ares.impl.util.EntityUtil
import org.aresclient.ares.impl.util.EntityUtil.PlayerThreat
import org.aresclient.ares.impl.util.EntityUtil.playerThreat
import org.aresclient.ares.impl.util.RenderPipelines
import org.aresclient.ares.impl.util.RenderUtil
import java.util.*
import kotlin.jvm.optionals.getOrNull

// TODO: FIX DEPTH ON OUTLINE ESP
// TODO: MAKE THIS MORE CUSTOMIZABLE + FRIENDS
object ESP: Module(Category.RENDER, "ESP", "See outlines of entities through walls") {
    enum class Mode { OUTLINE, BOX }

    private class EntityGroup(members: Set<Any> = emptySet()): Group<Any>(members) {
        val mode: EnumSetting<Mode> = addEnum("Mode", Mode.OUTLINE)
        val lineColor: ColorSetting = addColor("Line Color", Color.WHITE)
        val fillColor: ColorSetting = addColor("Fill Color", Color.WHITE)
    }

    private fun createEntityGroup(members: Collection<Any>, mode: Mode, color: Color, rainbow: Boolean = false, enabled: Boolean = true): EntityGroup {
        return EntityGroup(HashSet(members)).also {
            it.mode.value = mode
            it.lineColor.value = color
            it.fillColor.value = color.deriveAlpha(0.2f)
            it.fillColor.isRainbow = rainbow
            it.enabled.value = enabled
        }
    }

    private val entities = settings.addGrouped("Entities", arrayListOf(
        createEntityGroup(listOf(PlayerThreat.FRIEND), Mode.OUTLINE, Color.CYAN, rainbow = true),
        createEntityGroup(EntityUtil.Types.player.filter { it != PlayerThreat.FRIEND && it != PlayerThreat.BOT }, Mode.OUTLINE, Color.BLUE),
        createEntityGroup(EntityUtil.Types.monster, Mode.OUTLINE, EntityUtil.TargetType.HOSTILE.defaultColor),
        createEntityGroup(EntityUtil.Types.animal, Mode.OUTLINE, EntityUtil.TargetType.PASSIVE.defaultColor),
        createEntityGroup(EntityUtil.Types.miscellaneous.filter { it != EntityType.END_CRYSTAL && it != EntityType.ITEM }, Mode.OUTLINE, EntityUtil.TargetType.OTHER.defaultColor, enabled = false),
        createEntityGroup(listOf(EntityType.END_CRYSTAL), Mode.OUTLINE, EntityUtil.TargetType.END_CRYSTAL.defaultColor),
        createEntityGroup(listOf(EntityType.ITEM), Mode.BOX, EntityUtil.TargetType.ITEM.defaultColor)
    ), setOf(
        GroupMembers("Players", EntityUtil.Types.player.map { GroupMember("ares:player_${it.name.lowercase()}", it.name.formatToPretty(), it) }),
        GroupMembers("Monsters", EntityUtil.Types.monster.map { GroupMember(EntityType.getId(it).toString(), it.name.string, it) }),
        GroupMembers("Animals", EntityUtil.Types.animal.map { GroupMember(EntityType.getId(it).toString(), it.name.string, it) }),
        GroupMembers("Miscellaneous", EntityUtil.Types.miscellaneous.map { GroupMember(EntityType.getId(it).toString(), it.name.string, it) }),
    ), { EntityGroup() })
    private val entitiesCache = hashMapOf<Any, EntityGroup?>()
    private var shouldRenderOutlineCache: Boolean? = null

    override fun onTick() {
        entitiesCache.clear()
        shouldRenderOutlineCache = null
    }

    override fun onRenderWorld(delta: Float, renderer: Renderer.State) {
        WORLD.entities?.forEach { entity ->
            if(entity == SELF) return@forEach

            val group = getEntityGroup(entity) ?: return@forEach
            if(!group.enabled.value || group.mode.value != Mode.BOX) return@forEach

            val box = entity.getInterpolatedBoundingBox(delta)
            RenderUtil.Lines.box(box, group.lineColor.value, 2f)
            RenderUtil.Fill.box(box, group.fillColor.value)
        }
    }

    fun getEntityColor(entity: Entity): Color = getEntityGroup(entity)?.lineColor?.value ?: Color.COLORLESS

    fun shouldRenderOutline(): Boolean {
        if(!isEnabled()) return false
        if(shouldRenderOutlineCache != null) return shouldRenderOutlineCache!!
        shouldRenderOutlineCache = WORLD.entities.any { entity -> getEntityGroup(entity)?.let { it.enabled.value && it.mode.value == Mode.OUTLINE } == true }
        return shouldRenderOutlineCache!!
    }

    fun shouldRenderOutline(entity: Entity) = shouldRenderOutline() && getEntityGroup(entity)?.let { it.enabled.value && it.mode.value == Mode.OUTLINE } == true

    private fun getEntityGroup(entity: Entity): EntityGroup? {
        val type = if(entity is PlayerEntity) entity.playerThreat else entity.type as Any
        return entitiesCache.getOrPut(type) { entities.find(type).getOrNull() }
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
