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
import kotlin.math.cos
import kotlin.math.sin

object Freecam: Module(Category.PLAYER, "Freecam", "Allows the player to move the camera independently of the character"), CameraAdjustor {
	val speed = settings.addDouble("Speed", 1.0, "The speed at which the camera moves.")
		.setMin(0.0)
		.setPrecision(1)

	val matchCharacterRotation = settings.addBoolean("Match Char Rotation", true, "Use the same rotation as the player's character to turn the camera.")

	override fun priority(): Int = 100
	override fun cameraPosition(): Vec3d = position
	override fun cameraRotation(): Vec2f = rotation

	var position: Vec3d = Vec3d(0.0, 0.0, 0.0)
	var rotation: Vec2f = Vec2f(0F, 0F)

	var forward = false
	var backward = false
	var leftward = false
	var rightward = false
	var upward = false
	var downward = false

	override fun onEnable() {
		MC.gameRenderer.camera.let {
			position = it.pos
			rotation = Vec2f(it.yaw, it.pitch)
		}
		Camera.begin(this)
	}

	override fun onDisable() {
		Camera.end(this)
	}

	override fun onTick() {
		if (MC.world == null || MC.player == null) return

		val position = Camera.AresCamera.pos
		var yaw = Camera.AresCamera.yaw

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

		Camera.AresCamera.updateLastPosition()
		this.position = Vec3d(
			position.x - (if (isMovingLaterally()) sin(yaw.toDouble()) * speed else 0.0),
			position.y + if (upward) speed else if (downward) -speed else 0.0,
			position.z + (if (isMovingLaterally()) cos(yaw.toDouble()) * speed else 0.0),
		)

		if (matchCharacterRotation.value) {
			Camera.AresCamera.updateLastRotation()
			this.rotation = Vec2f(
				MC.player!!.yaw,
				MC.player!!.pitch
			)
		}
	}

	@field:EventHandler private val changeLookDirectionListener = EventListener<PlayerEvent.ChangeLookDirection> {
		if (matchCharacterRotation.value) return@EventListener

		it.isCancelled = true

		Camera.AresCamera.updateLastRotation()
		this.rotation = Vec2f(
			Camera.AresCamera.yaw + it.cursorDeltaX.toFloat() * 0.15F,
			Camera.AresCamera.pitch + it.cursorDeltaY.toFloat() * 0.15F
		)
	}

	fun isMovingLaterally(): Boolean = forward || backward || leftward || rightward

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

}