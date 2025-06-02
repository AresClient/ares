package org.aresclient.ares.impl.instrument.module.modules.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.Frustum
import net.minecraft.client.render.OutlineVertexConsumerProvider
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LightningEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.nrender.AresRenderPipelines
import org.aresclient.ares.api.nrender.world.WorldDrawer
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.api.setting.settings.EnumSetting
import org.aresclient.ares.api.setting.settings.grouped.Group
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.instrument.module.components.render.esp.Chamlike
import org.aresclient.ares.impl.util.EntityUtil
import org.aresclient.ares.impl.util.EntityUtil.PlayerThreat
import org.aresclient.ares.impl.util.EntityUtil.playerThreat
import org.aresclient.ares.mixin.accessors.AccessWorldRenderer
import java.util.*
import kotlin.jvm.optionals.getOrNull

// TODO: FIX DEPTH ON OUTLINE ESP
object ESP: Module(Category.RENDER, "ESP", "See outlines of entities through walls") {
    enum class Mode { OUTLINE, BOX, CHAMLIKE }

    init {
        Chamlike
    }

    class EntityGroup(members: Set<Any> = emptySet()): Group<Any>(members) {
        companion object {
            fun create(title: String, members: Collection<Any>, mode: Mode, target: EntityUtil.Target, enabled: Boolean = false): EntityGroup {
                return EntityGroup(HashSet(members)).also {
                    it.title.value = title
                    it.mode.value = mode
                    it.lineColor.value = target.defaultColor
                    it.lineColor.isRainbow = target.defaultRainbow
                    it.fillColor.value = target.defaultColor.deriveAlpha(0.2f)
                    it.fillColor.isRainbow = target.defaultRainbow
                    it.enabled.value = enabled
                }
            }
        }

        val mode: EnumSetting<Mode> = addEnum("Mode", Mode.OUTLINE)
        val lineColor: ColorSetting = addColor("Line Color", Color.WHITE)
        val fillColor: ColorSetting = addColor("Fill Color", Color.WHITE.deriveAlpha(0.2f)).setVisibility { mode.value != Mode.OUTLINE } as ColorSetting
    }

    private val entities = settings.addGrouped("Entities", arrayListOf(
        EntityGroup.create("Friends", listOf(PlayerThreat.FRIEND), Mode.OUTLINE, PlayerThreat.FRIEND, enabled = true),
        EntityGroup.create("Players", listOf(PlayerThreat.HOSTILE), Mode.OUTLINE, PlayerThreat.HOSTILE, enabled = true),
        EntityGroup.create("Crystals", listOf(EntityType.END_CRYSTAL), Mode.CHAMLIKE, EntityUtil.TargetType.END_CRYSTAL, enabled = true),
        EntityGroup.create("Monsters", EntityUtil.EntityTypes.monster, Mode.OUTLINE, EntityUtil.TargetType.HOSTILE),
        EntityGroup.create("Animals", EntityUtil.EntityTypes.animal, Mode.OUTLINE, EntityUtil.TargetType.PASSIVE),
        EntityGroup.create("Items", listOf(EntityType.ITEM), Mode.OUTLINE, EntityUtil.TargetType.ITEM, enabled = true),
        EntityGroup.create("Misc", EntityUtil.EntityTypes.miscellaneous.filter { it != EntityType.END_CRYSTAL && it != EntityType.ITEM }, Mode.OUTLINE, EntityUtil.TargetType.OTHER)
    ), EntityUtil.EntityTypes.possibles, { EntityGroup() })

    private val entitiesCache = hashMapOf<Any, EntityGroup?>()
    private var shouldRenderOutlineCache: Boolean? = null

    override fun onTick() {
        entitiesCache.clear()
        shouldRenderOutlineCache = null
    }

    override fun onRenderWorld(drawer: WorldDrawer, delta: Float) {
        Chamlike.onRenderWorld(drawer, delta)
        if(entities.none { it.enabled.value && it.mode.value == Mode.BOX }) return

        val frustum = Frustum(MC.worldRenderer.capturedFrustum ?: (MC.worldRenderer as AccessWorldRenderer).frustum)
        WORLD.entities?.forEach { entity ->
            if(entity == SELF) return@forEach

            val group = getEntityGroup(entity) ?: return@forEach
            if(!group.enabled.value || group.mode.value != Mode.BOX || entity.shouldCull(frustum)) return@forEach

            val box = entity.getInterpolatedBoundingBox(delta)

            drawer.fillBox(box, group.fillColor.value)
            drawer.outlineBox(box, group.lineColor.value, 2f)
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

    fun getEntityGroup(entity: Entity): EntityGroup? {
        val type = if(entity is PlayerEntity) entity.playerThreat else entity.type as Any
        return entitiesCache.getOrPut(type) { entities.find(type).getOrNull() }
    }

    private fun <T: Entity> T.shouldCull(frustum: Frustum): Boolean {
        if(this is DisplayEntity && !shouldRender()) return true
        return when(this) {
            is EnderDragonEntity, is FishingBobberEntity, is LightningEntity -> false
            else -> !frustum.isVisible(boundingBox.expand(0.5))
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
                renderPass.setPipeline(AresRenderPipelines.OUTLINE)
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
