package org.aresclient.ares.impl.instrument.module.modules.player

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.events.InputEvent
import org.aresclient.ares.api.events.InputEvent.Keyboard.Pressed
import org.aresclient.ares.api.events.PlayerEvent
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.impl.instrument.global.Camera
import org.aresclient.ares.impl.instrument.global.CameraAdjustor
import org.aresclient.ares.impl.util.MathUtil._x
import org.aresclient.ares.impl.util.MathUtil._y
import org.aresclient.ares.impl.util.MathUtil._z
import org.aresclient.ares.impl.util.MathUtil.duplicate
import org.aresclient.ares.impl.util.MathUtil.moveCameraWithCursor
import org.aresclient.ares.impl.util.MathUtil.set
import kotlin.math.cos
import kotlin.math.sin

object Freecam: Module(Category.PLAYER, "Freecam", "Allows the player to move the camera independently of the character"), CameraAdjustor {

	private val speed = settings.addDouble("Speed", 1.0, "The speed at which the camera moves.")
		.setMin(0.0)
		.setPrecision(1)

	// ════════════════════════════════════════════════════════════════════════ //

	override fun priority(): Int = 100
	override val cameraPosition: Vec3d = Vec3d.ZERO.duplicate()
	override val cameraRotation: Vec2f = Vec2f.ZERO.duplicate()
	override val shouldRenderCharacter: Boolean = true

	// ════════════════════════════════════════════════════════════════════════ //

	var forward = false
	var backward = false
	var leftward = false
	var rightward = false
	var upward = false
	var downward = false

	// ════════════════════════════════════════════════════════════════════════ //

	override fun onEnable() {
		MC.gameRenderer.camera.let {
			cameraPosition.set(it.pos)
			cameraRotation.set(it.yaw, it.pitch)
		}
		Camera.begin(this)
	}

	override fun onDisable() {
		Camera.end(this)
	}

	override fun onTick() {
		if (MC.world == null || MC.player == null) return

		var yaw = MC.gameRenderer.camera.yaw

		var speed = speed.value
		if (!MC.options.sprintKey.isPressed) speed *= 0.5

		var forwardValue = 1F
		if (backward) {
			yaw += 180
			forwardValue = -0.5F
		} else if (forward) forwardValue = 0.5F

		if (leftward) yaw -= 90 * forwardValue
		if (rightward) yaw += 90 * forwardValue

		yaw = Math.toRadians(yaw.toDouble()).toFloat()

		cameraPosition._x -= if (isMovingLaterally()) sin(yaw.toDouble()) * speed else 0.0
		cameraPosition._y += if (upward) speed else if (downward) -speed else 0.0
		cameraPosition._z += if (isMovingLaterally()) cos(yaw.toDouble()) * speed else 0.0
	}

	@field:EventHandler private val changeLookDirectionListener = EventListener<PlayerEvent.ChangeLookDirection> { event ->
		if (!Camera.hasPriority(this)) return@EventListener
		event.isCancelled = true
		cameraRotation.moveCameraWithCursor(event)
	}

	@field:EventHandler private val keyboardListener = EventListener<InputEvent.Keyboard> { event ->
		val opt = MC.options

		if (opt.forwardKey.matchesKey(event.key, 0)) {
			opt.forwardKey.isPressed = false
			forward = event is Pressed
		} else if (opt.backKey.matchesKey(event.key, 0)) {
			opt.backKey.isPressed = false
			backward = event is Pressed
		} else if (opt.leftKey.matchesKey(event.key, 0)) {
			opt.leftKey.isPressed = false
			leftward = event is Pressed
		} else if (opt.rightKey.matchesKey(event.key, 0)) {
			opt.rightKey.isPressed = false
			rightward = event is Pressed
		} else if (opt.jumpKey.matchesKey(event.key, 0)) {
			opt.jumpKey.isPressed = false
			upward = event is Pressed
		} else if (opt.sneakKey.matchesKey(event.key, 0)) {
			opt.sneakKey.isPressed = false
			downward = event is Pressed
		} else return@EventListener

		event.isCancelled = true
	}

	// ════════════════════════════════════════════════════════════════════════ //

	private fun isMovingLaterally(): Boolean = forward || backward || leftward || rightward
}
