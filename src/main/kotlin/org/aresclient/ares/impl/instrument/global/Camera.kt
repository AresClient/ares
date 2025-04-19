package org.aresclient.ares.impl.instrument.global

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import dev.tigr.simpleevents.listener.Priority
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.events.CameraEvent
import org.aresclient.ares.api.events.Era
import org.aresclient.ares.api.events.TickEvent
import org.aresclient.ares.api.instruments.Global
import org.aresclient.ares.api.instruments.Prioritizer
import org.aresclient.ares.impl.util.MathUtil.duplicate
import org.aresclient.ares.impl.util.MathUtil.set

interface CameraAdjustor: Prioritizer {
	val cameraPosition: Vec3d?
	val cameraRotation: Vec2f?
	val shouldRenderCharacter: Boolean
}

object Camera: Global.PriorityHandler<CameraAdjustor>("Camera", "Manages camera interactions") {

	override fun begin() {
		lastPosition.set(MC.gameRenderer.camera.pos)
		lastRotation.set(MC.gameRenderer.camera.yaw, MC.gameRenderer.camera.pitch)
	}

	override fun end() {
		lastPosition.set(Vec3d.ZERO)
		lastRotation.set(Vec2f.ZERO)
	}

	// ════════════════════════════════════════════════════════════════════════ //

	private val lastPosition = Vec3d.ZERO.duplicate()
	private val lastRotation = Vec2f.ZERO.duplicate()

	// ════════════════════════════════════════════════════════════════════════ //

	// Run on tick event with higher priority than default tick so that this runs before default tick events
	@field:EventHandler private val tickEvent = EventListener<TickEvent.Client>(Priority.HIGH) { event ->
		if (event.era != Era.BEFORE) return@EventListener
		val current = getCurrent() ?: return@EventListener
		lastPosition.set(current.cameraPosition ?: MC.gameRenderer.camera.pos)
	}

	@field:EventHandler private val onCameraUpdate = EventListener<CameraEvent> { event ->
		val current = getCurrent() ?: return@EventListener

		when (event) {
			is CameraEvent.Position -> {
				val currentPosition: Vec3d = current.cameraPosition ?: return@EventListener
				event.x = MathHelper.lerp(event.delta.toDouble(), lastPosition.x, currentPosition.x)
				event.y = MathHelper.lerp(event.delta.toDouble(), lastPosition.y, currentPosition.y)
				event.z = MathHelper.lerp(event.delta.toDouble(), lastPosition.z, currentPosition.z)
			}
			is CameraEvent.Rotation -> {
				val currentRotation: Vec2f = current.cameraRotation ?: return@EventListener
				event.yaw = currentRotation.x
				event.pitch = currentRotation.y
			}
		}

		event.isCancelled = true
	}

	// ════════════════════════════════════════════════════════════════════════ //

	fun shouldRenderCharacter(): Boolean? {
		val current = getCurrent() ?: return null
		val cameraPosition = current.cameraPosition ?: return null
		val player = MC.player ?: return false
		return (if (current.shouldRenderCharacter) true else MC.gameRenderer.camera.isThirdPerson)
				&& !player.boundingBox.intersects(cameraPosition, cameraPosition)
	}
}