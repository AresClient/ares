package org.aresclient.ares.impl.instrument.global

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.events.PlayerEvent
import org.aresclient.ares.api.instruments.Global
import org.aresclient.ares.api.instruments.Prioritizer
import org.aresclient.ares.impl.util.EntityUtil.rotation
import org.aresclient.ares.impl.util.MathUtil._x
import org.aresclient.ares.impl.util.MathUtil._y
import org.aresclient.ares.impl.util.MathUtil.duplicate
import org.aresclient.ares.impl.util.MathUtil.getAngleDifference
import org.aresclient.ares.impl.util.MathUtil.moveCameraWithCursor
import org.aresclient.ares.impl.util.MathUtil.normalizeRotation
import org.aresclient.ares.impl.util.MathUtil.set
import org.aresclient.ares.impl.util.Timer
import kotlin.math.min

interface Rotator: Prioritizer {
	val yawStep: Float get() = Rotation.yaw_step.value
	val pitchStep: Float get() = Rotation.pitch_step.value
	val rotation: Vec2f
}

object Rotation: Global.PriorityHandler<Rotator>("Rotation", "Handles rotation so that the character is facing in the expected direction for an action."), CameraAdjustor {

	private val reset_delay = settings.addLong("Reset Delay", 10)
		.setDescription("How long to wait after completing a rotation before resetting to the same rotation as the camera.")
		.setMin(0)
		.setMax(100)

	private val completion_delay = settings.addLong("Completion Delay", 1)
		.setDescription("How long to wait after completing a rotation before interactions can happen.")
		.setMin(0)
		.setMax(10)

	private val grouping_density = settings.addFloat("Grouping Density", 0F)
		.setDescription("The rotation distance in degrees within which to ignore the completion delay.")
		.setMin(0F)
		.setMax(180F)

	internal val yaw_step = settings.addFloat("Yaw Step", 180F)
		.setDescription("How many degrees to turn horizontally per tick.")
		.setMin(1F)
		.setMax(180F)

	internal val pitch_step = settings.addFloat("Pitch Step", 180F)
		.setDescription("How many degrees to turn vertically per tick.")
		.setMin(1F)
		.setMax(180F)

	// ════════════════════════════════════════════════════════════════════════ //

	override fun begin() {
		cameraRotation!!.set(MC.gameRenderer.camera.yaw, MC.gameRenderer.camera.pitch)
		lastRotation.set(MC.player?.yaw ?: 0F, MC.player?.pitch ?: 0F)
	}

	override fun end() {
		MC.player!!.rotation = cameraRotation
		Camera.end(this)
	}

	override val cameraPosition: Vec3d? = null
	override val cameraRotation: Vec2f = Vec2f.ZERO.duplicate()
	override val shouldRenderCharacter: Boolean = false
	override fun priority(): Int = 1

	// ════════════════════════════════════════════════════════════════════════ //

	val currentRotation = Vec2f.ZERO.duplicate()
	val lastRotation = Vec2f.ZERO.duplicate()
	val resetTimer = Timer()
	var steppingComplete = true

	// ════════════════════════════════════════════════════════════════════════ //

	override fun tick() {
		if (keys.isEmpty() && Camera.isActive(this) && resetTimer.hasTicksPassed(reset_delay.value)) {
			Camera.end(this)
			resetTimer.reset()
			return
		}

		if (MC.world == null || MC.player == null || keys.isEmpty()) {
			resetTimer.reset()
			return
		}

		val current = getCurrent() ?: return
		currentRotation.set(current.rotation).normalizeRotation()
		Camera.begin(this)

		val yawStep = min(current.yawStep, yaw_step.value)
		val pitchStep = min(current.pitchStep, pitch_step.value)

		if (!lastRotation.equals(currentRotation) && yawStep != 180F || pitchStep != 180F) {
			val xChange = lastRotation.x.getAngleDifference(currentRotation.x)
			val yChange = lastRotation.y.getAngleDifference(currentRotation.y)
			currentRotation._x = steppedAngle(xChange, yawStep, lastRotation.x, currentRotation.x)
			currentRotation._y = steppedAngle(yChange, pitchStep, lastRotation.y, currentRotation.y)
		} else steppingComplete = true

		MC.player!!.rotation = currentRotation

		lastRotation.set(currentRotation)
	}

	@field:EventHandler private val changeLookDirection = EventListener<PlayerEvent.ChangeLookDirection> { event ->
		if (!isRotating) return@EventListener
		if (Camera.hasPriority(this)) event.isCancelled = true
		cameraRotation.moveCameraWithCursor(event)
	}

	@field:EventHandler private val updateVelocityYaw = EventListener<PlayerEvent.UpdateVelocityYaw> { event ->
		if (!Camera.hasPriority(this)) return@EventListener

		event.isCancelled = true
		event.yaw = cameraRotation.x
	}

	// ════════════════════════════════════════════════════════════════════════ //

	private fun steppedAngle(change: Float, step: Float, last: Float, current: Float): Float =
		if (change > step) {
			steppingComplete = false
			last +step
		}
		else if(change < -step) {
			steppingComplete = false
			last -step
		}
		else {
			steppingComplete = true
			current
		}

	val isRotating: Boolean get() = Camera.isActive(this)
}

