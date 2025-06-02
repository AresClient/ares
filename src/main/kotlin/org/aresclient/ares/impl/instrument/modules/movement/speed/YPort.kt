package org.aresclient.ares.impl.instrument.modules.movement.speed

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.events.PlayerEvent
import org.aresclient.ares.api.instruments.Component
import org.aresclient.ares.impl.instrument.modules.movement.speed.Speed.changeMovement
import org.aresclient.ares.impl.util.MathUtil._x
import org.aresclient.ares.impl.util.MathUtil._z

object YPort: Component<Speed>(Speed), Wrapper {

	private var phase = Phase.START

	@field:EventHandler private val moveEventListener = EventListener<PlayerEvent.Move> { event ->
		if (parent.mode.value != Speed.Mode.Y_PORT || MC.NULL) return@EventListener

		parent.v = 0.2873
		parent.y = SELF.velocity.y

		if (SELF.horizontalCollision) {
			phase = Phase.START
		}

		if (SELF.forwardSpeed == 0F && SELF.sidewaysSpeed == 0F) {
			reset()
			parent.speed = 0.0
			event.movement._x = 0.0
			event.movement._z = 0.0
			event.isCancelled = true
			return@EventListener
		}

		phase.action.invoke()
		event.changeMovement()
	}

	private enum class Phase(val action: () -> Unit) {
		START(YPort::start),
		MIDDLE(YPort::middle),
		END(YPort::end)
	}

	fun reset() {
		phase = Phase.START
	}

	private fun start() {
		phase = Phase.MIDDLE
		parent.y = -1.0
		parent.speed = parent.v
	}

	private fun middle() {
		parent.speed *= 2.149
		parent.y = 0.42
		phase = Phase.END
	}

	private fun end() {
		parent.speed = parent.lastDist - 0.66 * (parent.lastDist - parent.v)
		phase = Phase.MIDDLE
		parent.y = -1.0
	}

}