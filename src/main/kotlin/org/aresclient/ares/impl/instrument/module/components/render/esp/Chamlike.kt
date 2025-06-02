package org.aresclient.ares.impl.instrument.module.components.render.esp

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.state.EntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.item.Item
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.events.RenderEntityLabelEvent
import org.aresclient.ares.api.instruments.Component
import org.aresclient.ares.api.nrender.world.WorldDrawer
import org.aresclient.ares.impl.instrument.module.modules.render.ESP
import org.aresclient.ares.impl.util.MathUtil.duplicate
import org.aresclient.ares.impl.util.MathUtil.set

object Chamlike: Component<ESP>(ESP), Wrapper {
	/** @see org.aresclient.ares.mixin.mixins.MixinItemRenderer.onRenderItem */
	var isBlockItem = false
	var active = false

	private var x = 0.0
	private var y = 0.0
	private var z = 0.0

	private lateinit var entityGroup: ESP.EntityGroup
	private lateinit var worldDrawer: WorldDrawer
	private val matrixStack = MatrixStack()

	fun onRenderWorld(drawer: WorldDrawer, tickDelta: Float) {
		worldDrawer = drawer
		active = true

		for (entity in WORLD.entities) {
			if (entity == SELF) continue

			isBlockItem = entity is ItemEntity && Item.BLOCK_ITEMS.containsValue(entity.stack.item)

			entityGroup = ESP.getEntityGroup(entity) ?: continue
			if (!entityGroup.enabled.value || entityGroup.mode.value != ESP.Mode.CHAMLIKE) continue

			val entityRenderer = MC.entityRenderDispatcher.getRenderer(entity) as EntityRenderer<Entity, EntityRenderState>
			val entityState = entityRenderer.getAndUpdateRenderState(entity, tickDelta)
			val position = entityRenderer.getPositionOffset(entityState)

			x = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderX, entity.x) + position.x
			y = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderY, entity.y) + position.y
			z = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderZ, entity.z) + position.z

			entityRenderer.render(entityState, matrixStack, ChamlikeVertexProvider, 0)
		}

		active = false
	}

	@field:EventHandler private val renderEntityLabelEvent = EventListener<RenderEntityLabelEvent> {
		if (active) it.isCancelled = true
	}

	private object ChamlikeVertexProvider: VertexConsumerProvider {
		private val stored = HashMap<RenderLayer, ChamlikeVertexConsumer>()
		override fun getBuffer(layer: RenderLayer?): ChamlikeVertexConsumer = stored.getOrPut(layer!!) { ChamlikeVertexConsumer() }
	}

	private class ChamlikeVertexConsumer: VertexConsumer {
		private val vertices = List(4) { Vec3d.ZERO.duplicate() }.toTypedArray()
		private var i = 0

		override fun vertex(x: Float, y: Float, z: Float): VertexConsumer {
			vertices[i].set(x.offsetX(), y.offsetY(), z.offsetZ())
			if(++i != 4) return this

			worldDrawer.fillQuad(vertices[0], vertices[1], vertices[2], vertices[3], entityGroup.fillColor.value)
			worldDrawer.outlineQuad(vertices[0], vertices[1], vertices[2], vertices[3], entityGroup.lineColor.value, 1f)

			i = 0
			return this
		}

		override fun color(red: Int, green: Int, blue: Int, alpha: Int): VertexConsumer = this
		override fun texture(u: Float, v: Float): VertexConsumer = this
		override fun overlay(u: Int, v: Int): VertexConsumer = this
		override fun light(u: Int, v: Int): VertexConsumer = this
		override fun normal(x: Float, y: Float, z: Float): VertexConsumer = this
	}

	private fun Float.offsetX(): Double = x + this - CAMERA.pos.x
	private fun Float.offsetY(): Double = y + this - CAMERA.pos.y
	private fun Float.offsetZ(): Double = z + this - CAMERA.pos.z
}
