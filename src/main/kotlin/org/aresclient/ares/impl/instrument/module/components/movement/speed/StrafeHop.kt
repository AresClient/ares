package org.aresclient.ares.impl.instrument.module.components.movement.speed

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.minecraft.entity.effect.StatusEffects
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.events.PlayerEvent
import org.aresclient.ares.api.instruments.Component
import org.aresclient.ares.impl.instrument.module.modules.movement.Speed
import org.aresclient.ares.impl.instrument.module.modules.movement.Speed.changeMovement
import org.aresclient.ares.impl.util.MathUtil._x
import org.aresclient.ares.impl.util.MathUtil._z

object StrafeHop: Component.Settings<Speed>(Speed, "Strafe Hop"), Wrapper {

	init {
		settings.setDescription("Settings for Strafe Hop mode")
		settings.setVisibility { Speed.mode.value == Speed.Mode.STRAFE_HOP }
	}

	private val strict = settings.addBoolean("Strict", true, "Whether to use NCP strict speed or not.")

	private var phase = Phase.START

	@field:EventHandler private val moveEventListener = EventListener<PlayerEvent.Move> { event ->
		if (master.mode.value != Speed.Mode.STRAFE_HOP || MC.NULL) return@EventListener

		master.v = 0.2873
		master.y = SELF.velocity.y

		if (SELF.activeStatusEffects.containsValue(SELF.getStatusEffect(StatusEffects.SPEED))) {
			val amplifier = SELF.getStatusEffect(StatusEffects.SPEED)?.amplifier ?: 0
			master.v *= 1.0 + 0.2 * (amplifier + 1)
		}

		if (SELF.forwardSpeed == 0F && SELF.sidewaysSpeed == 0F) {
			phase = Phase.START
			master.speed = 0.0
			event.movement._x = 0.0
			event.movement._z = 0.0
			event.isCancelled = true
			return@EventListener
		}

		if (SELF.isOnGround || SELF.horizontalCollision) {
			master.speed = 0.0
			reset()
		}

		LOGGER.info("INVOKING STRAFE")
		phase.action.invoke()
		event.changeMovement()
	}

	fun reset() {
		phase = Phase.START
	}

	private enum class Phase(val action: () -> Unit) {
		START(StrafeHop::start),
		MIDDLE(StrafeHop::middle),
		END(StrafeHop::end)
	}

	private fun start() {
		if (!SELF.isOnGround) return

		master.y = 0.42
		master.speed = master.v * (if (strict.value) 1.87 else 1.91)
		phase = Phase.MIDDLE
	}

	private fun middle() {
		master.speed -= 0.66 * master.v
		phase = Phase.END
	}

	private fun end() {
		master.speed = master.lastDist - master.lastDist / 159
	}

}