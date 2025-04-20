package org.aresclient.ares.impl.instrument.module.modules.movement

import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.impl.instrument.global.Rotation
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Strafe: Module(Category.MOVEMENT, "Strafe", "") {
	val lowHop = settings.addBoolean("Low Hop", false)!!
	val height = settings
		.addDouble("Height", 0.3)
		.setMin(0.3)
		.setMax(0.5)
		.setVisibility(lowHop::getValue)!!
	val speedBool = settings.addBoolean("Modify Speed", false)!!
	val speedVal = settings
		.addDouble("Speed", 0.32)
		.setMin(0.2)
		.setMax(0.6)
		.setVisibility(speedBool::getValue)!!
	val sprintBool = settings.addBoolean("Auto Sprint", true)!!

	override fun onMotion() {
		val player = MC.player ?: return
		val input = player.input?.playerInput ?: return
		if (!input.forward && !input.backward && !input.left && !input.right) return

		if (sprintBool.value) player.setSprinting(true)
		if (player.isOnGround) {
			if (lowHop.value) player.addVelocity(0.0, height.value, 0.0)
			return
		}

		val speed =
			if (!speedBool.value) sqrt(player.velocity.x * player.velocity.x + player.velocity.z * player.velocity.z)
			else speedVal.value

		var yaw: Float = if (Rotation.isRotating) MC.gameRenderer.camera.yaw else player.yaw
		var forward = 1f
		if (player.forwardSpeed < 0) {
			yaw += 180f
			forward = -0.5f
		}
		else if (player.forwardSpeed > 0) forward = 0.5f
		if (player.sidewaysSpeed > 0) yaw -= 90 * forward
		if (player.sidewaysSpeed < 0) yaw += 90 * forward
		yaw = Math
			.toRadians(yaw.toDouble())
			.toFloat()

		player.setVelocity(-sin(yaw.toDouble()) * speed, player.velocity.y, cos(yaw.toDouble()) * speed)
	}
}