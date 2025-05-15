package org.aresclient.ares.impl.instrument.module.modules.player

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.minecraft.client.option.Perspective
import net.minecraft.util.math.MathHelper
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
import org.aresclient.ares.impl.util.MathUtil.toTransverseMovement

object Freecam: Module(Category.PLAYER, "Freecam", "Allows the player to move the camera independently of the character", Defaults().setExternalToggleList(true)), CameraAdjustor {

	private val speed = settings.addDouble("Speed", 1.0, "The speed at which the camera moves.")
		.setMin(0.0)
		.setPrecision(1)

	private val scroll = settings.addBoolean("Scroll", true, "Allow speed to be adjusted using scroll wheel")

	// ════════════════════════════════════════════════════════════════════════ //

	override fun priority(): Int = 100
	override val cameraPosition: Vec3d = Vec3d.ZERO.duplicate()
	override val cameraRotation: Vec2f = Vec2f.ZERO.duplicate()
	override val shouldRenderCharacter: Boolean = true

	// ════════════════════════════════════════════════════════════════════════ //

	private var prevPerspective: Perspective? = null

	private var forward = false
	private var backward = false
	private var leftward = false
	private var rightward = false
	private var upward = false
	private var downward = false

	// ════════════════════════════════════════════════════════════════════════ //

	override fun onEnable() {
		if (MC.NULL) {
			setEnabled(false)
			return
		}

		CAMERA.let {
			cameraPosition.set(it.pos)
			cameraRotation.set(it.yaw, it.pitch)
		}

		with(MC.options) {
			prevPerspective = perspective

			forward = forwardKey.isPressed
			backward = backKey.isPressed
			leftward = leftKey.isPressed
			rightward = rightKey.isPressed
			upward = jumpKey.isPressed
			downward = sneakKey.isPressed

			perspective = Perspective.FIRST_PERSON
			forwardKey.isPressed = false
			backKey.isPressed = false
			leftKey.isPressed = false
			rightKey.isPressed = false
			sneakKey.isPressed = false
			jumpKey.isPressed = false
		}

		Camera.begin(this)
	}

	override fun onDisable() {
		Camera.end(this)

		if(prevPerspective != null) {
			MC.options.perspective = prevPerspective!!
			prevPerspective = null
		}

		with(MC.options) {
			forwardKey.isPressed = forward
			backKey.isPressed = backward
			leftKey.isPressed = leftward
			rightKey.isPressed = rightward
			jumpKey.isPressed = upward
			sneakKey.isPressed = downward
		}
	}

	override fun onTick() {
		if (MC.NULL) {
			setEnabled(false)
			return
		}

		var speed = speed.value
		if (!MC.options.sprintKey.isPressed) speed *= 0.5

		val transverseMovement = MC.gameRenderer.camera.yaw.toTransverseMovement(
			speed,
			if (forward && !backward) 1F else if (backward && !forward) -1F else 0F,
			if (leftward && !rightward) 1F else if (rightward && !leftward) -1F else 0F
		)

		cameraPosition._x += transverseMovement.x
		cameraPosition._y += if (upward) speed else if (downward) -speed else 0.0
		cameraPosition._z += transverseMovement.y
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
		} else if (!opt.togglePerspectiveKey.matchesKey(event.key, 0)) return@EventListener

		event.isCancelled = true
	}

	@field:EventHandler private val mouseListener = EventListener<InputEvent.Mouse.Scrolled> { event ->
		if(scroll.value) {
			speed.value = MathHelper.clamp(speed.value + event.vertical / 2.0, 0.2, 80.0)
			event.isCancelled = true
		}
	}

	// ════════════════════════════════════════════════════════════════════════ //

}
