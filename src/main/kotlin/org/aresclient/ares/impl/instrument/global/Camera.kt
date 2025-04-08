package org.aresclient.ares.impl.instrument.global

import net.minecraft.client.render.Camera
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.vehicle.ExperimentalMinecartController
import net.minecraft.entity.vehicle.MinecartEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import org.aresclient.ares.api.instruments.Global
import org.aresclient.ares.impl.instrument.global.Camera.AresCamera.lastPosition
import org.aresclient.ares.impl.instrument.global.Camera.AresCamera.lastRotation
import org.aresclient.ares.mixin.accessors.AccessCamera
import org.aresclient.ares.mixin.accessors.AccessGameRenderer

interface CameraAdjustor: Prioritizer {
	fun cameraPosition(): Vec3d
	fun cameraRotation(): Vec2f
}

object Camera: Global("Camera", "Manages camera interactions") {
	private var adjustors:HashSet<CameraAdjustor> = HashSet()
	private var originalCamera: Camera? = null

	fun begin(key: CameraAdjustor): Boolean {
		if (adjustors.isNotEmpty()) {
			if (!adjustors.contains(key)) adjustors.add(key)
			return hasPriority(key)
		}

		adjustors.add(key)

		lastPosition = MC.gameRenderer.camera.pos
		(AresCamera as AccessCamera).setPos(lastPosition!!.x, lastPosition!!.y, lastPosition!!.z)

		lastRotation = Vec2f(MC.gameRenderer.camera.yaw, MC.gameRenderer.camera.pitch)
		(AresCamera as AccessCamera).setRotation(lastRotation!!.x, lastRotation!!.y)

		originalCamera = MC.gameRenderer.camera
		(MC.gameRenderer as AccessGameRenderer).setCamera(AresCamera)

		return true
	}

	fun end(key: CameraAdjustor) {
		if (adjustors.contains(key)) adjustors.remove(key)
		if (adjustors.isEmpty()) end()
	}

	fun hasPriority(key: CameraAdjustor): Boolean {
		return key == getNext()
	}

	internal fun end() {
		lastPosition = null
		lastPosition = null

		(MC.gameRenderer as AccessGameRenderer).setCamera(originalCamera)
		originalCamera = null
	}

	internal fun getNext(): CameraAdjustor? {
		var key: CameraAdjustor? = null
		adjustors.forEach {
			if (key == null) {
				key = it
				return@forEach
			}

			if (key!!.priority() < it.priority()) key = it
		}
		return key
	}

	object AresCamera: Camera() {
		internal var lastPosition: Vec3d? = null
		internal var lastRotation: Vec2f? = null

		override fun update(area: BlockView?, focusedEntity: Entity?, thirdPerson: Boolean, inverseView: Boolean, tickProgress: Float) {
			this as AccessCamera
			this.setReady(true)
			this.setArea(area)
			this.setFocusedEntity(focusedEntity)
			this.setThirdPerson(true)
			this.setLastTickProgress(tickProgress)

			val next = getNext()

			if (next == null) { // Should never be null, but just in case
				end()
				MC.gameRenderer.camera.update(area, focusedEntity, thirdPerson, inverseView, tickProgress)
				return
			}

			applyCameraTransformations(tickProgress, next.cameraPosition(), next.cameraRotation())
		}

		private fun applyCameraTransformations(tickProgress:Float, position:Vec3d, rotation:Vec2f) {
			val lastPosition = lastPosition!!; val lastRotation = lastRotation!!

			setPos(
				MathHelper.lerp(tickProgress.toDouble(), lastPosition.x, position.x),
				MathHelper.lerp(tickProgress.toDouble(), lastPosition.y, position.y),
				MathHelper.lerp(tickProgress.toDouble(), lastPosition.z, position.z)
			)
			setRotation(
				if (tickProgress == 1F) rotation.x else MathHelper.lerpAngleDegrees(tickProgress, lastRotation.x, rotation.x),
				if (tickProgress == 1F) rotation.y else MathHelper.lerp(tickProgress, lastRotation.y, rotation.y)
			)
		}

		fun updateLastPosition() {
			lastPosition = pos
		}

		fun updateLastRotation() {
			lastRotation = Vec2f(yaw, pitch)
		}

		internal fun unmodified(inverseView: Boolean) {
			this as AccessCamera
			val tp = lastTickProgress
			val fe = focusedEntity

			(fe.vehicle as? MinecartEntity)?.let { me ->
				(me.controller as? ExperimentalMinecartController)?.let { emc ->
					if (emc.hasCurrentLerpSteps()) return@let null

					val vec3d = me
						.getPassengerRidingPos(focusedEntity)
						.subtract(me.getPos())
						.subtract(fe.getVehicleAttachmentPos(me))
						.add(
							Vec3d(0.0, MathHelper
								.lerp(tp, this.lastCameraY, this.cameraY).toDouble(), 0.0)
						)

					setRotation(fe.getYaw(tp), fe.getPitch(tp))
					setPos(emc.getLerpedPosition(tp).add(vec3d))
				}
			} ?: {
				setRotation(fe.getYaw(tp), fe.getPitch(tp))
				setPos(
					MathHelper.lerp(tp.toDouble(), fe.lastX, fe.x),
					MathHelper.lerp(tp.toDouble(), fe.lastY, fe.y) + MathHelper.lerp(tp, lastCameraY, cameraY),
					MathHelper.lerp(tp.toDouble(), fe.lastZ, fe.z)
				)
			}

			if (thirdPerson) {
				if (inverseView) setRotation(yaw + 180f, -pitch)
				val f = (fe as? LivingEntity)?.let { it.scale } ?: 1F
				moveBy(-doClipToSpace(4f * f), 0f, 0f)
			} else if (fe is LivingEntity && fe.isSleeping) {
				val dir = fe.sleepingDirection
				setRotation(if (dir != null) dir.positiveHorizontalDegrees - 180f else 0f, 0f)
				moveBy(0f, 0.3f, 0f)
			}
		}
	}

}