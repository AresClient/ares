package org.aresclient.ares.impl.instrument.modules.movement.speed

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.minecraft.entity.effect.StatusEffects
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.events.PlayerEvent
import org.aresclient.ares.api.instruments.Component
import org.aresclient.ares.impl.instrument.modules.movement.speed.Speed.changeMovement
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
		if (Speed.mode.value != Speed.Mode.STRAFE_HOP || MC.NULL) return@EventListener

		Speed.v = 0.2873
		Speed.y = SELF.velocity.y

		if (SELF.activeStatusEffects.containsValue(SELF.getStatusEffect(StatusEffects.SPEED))) {
			val amplifier = SELF.getStatusEffect(StatusEffects.SPEED)?.amplifier ?: 0
			Speed.v *= 1.0 + 0.2 * (amplifier + 1)
		}

		if (SELF.forwardSpeed == 0F && SELF.sidewaysSpeed == 0F) {
			phase =
				Phase.START
			Speed.speed = 0.0
			event.movement._x = 0.0
			event.movement._z = 0.0
			event.isCancelled = true
			return@EventListener
		}

		if (SELF.isOnGround || SELF.horizontalCollision) {
			Speed.speed = 0.0
			reset()
		}

		phase.action.invoke()
		event.changeMovement()
	}

	fun reset() {
		phase =
			Phase.START
	}

	private enum class Phase(val action: () -> Unit) {
		START(StrafeHop::start),
		MIDDLE(StrafeHop::middle),
		END(StrafeHop::end)
	}

	private fun start() {
		if (!SELF.isOnGround) return

		Speed.y = 0.42
		Speed.speed = Speed.v * (if (strict.value) 1.87 else 1.91)
		phase =
			Phase.MIDDLE
	}

	private fun middle() {
		Speed.speed -= 0.66 * Speed.v
		phase =
			Phase.END
	}

	private fun end() {
		Speed.speed = Speed.lastDist - Speed.lastDist / 159
	}

}