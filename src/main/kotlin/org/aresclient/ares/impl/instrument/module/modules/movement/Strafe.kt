package org.aresclient.ares.impl.instrument.module.modules.movement

import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.impl.util.EntityUtil.withTransverseMovement
import kotlin.math.sqrt

object Strafe: Module(Category.MOVEMENT, "Strafe", "") {

	private val low_hop = settings
		.addBoolean("Low Hop", false)

	private val height = settings
		.addDouble("Height", 0.3)
		.setMin(0.3)
		.setMax(0.5)
		.setVisibility(low_hop::getValue)

	private val modify_speed = settings
		.addBoolean("Modify Speed", false)

	private val speed = settings
		.addDouble("Speed", 0.32)
		.setMin(0.2)
		.setMax(0.6)
		.setVisibility(modify_speed::getValue)

	private val sprint = settings
		.addBoolean("Auto Sprint", true)

	override fun onMotion() {
		val player = MC.player ?: return
		val input = player.input?.playerInput ?: return
		if (!input.forward && !input.backward && !input.left && !input.right) return

		if (sprint.value) player.setSprinting(true)
		if (player.isOnGround) {
			if (low_hop.value) player.addVelocity(0.0, height.value, 0.0)
			return
		}

		val speed =
			if (!modify_speed.value) sqrt(player.velocity.x * player.velocity.x + player.velocity.z * player.velocity.z)
			else speed.value

		player.withTransverseMovement(speed)
	}
}