package org.aresclient.ares.impl.instrument.module.components.movement.speed

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.events.PlayerEvent
import org.aresclient.ares.api.instruments.Component
import org.aresclient.ares.impl.instrument.module.modules.movement.Speed
import org.aresclient.ares.impl.instrument.module.modules.movement.Speed.changeMovement
import org.aresclient.ares.impl.util.MathUtil._x
import org.aresclient.ares.impl.util.MathUtil._z

object YPort: Component<Speed>(Speed), Wrapper {

	private var phase = Phase.START

	@field:EventHandler private val moveEventListener = EventListener<PlayerEvent.Move> { event ->
		if (master.mode.value != Speed.Mode.Y_PORT || MC.NULL) return@EventListener

		master.v = 0.2873
		master.y = SELF.velocity.y

		if (SELF.horizontalCollision) {
			phase = Phase.START
		}

		if (SELF.forwardSpeed == 0F && SELF.sidewaysSpeed == 0F) {
			reset()
			master.speed = 0.0
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
		master.y = -1.0
		master.speed = master.v
	}

	private fun middle() {
		master.speed *= 2.149
		master.y = 0.42
		phase = Phase.END
	}

	private fun end() {
		master.speed = master.lastDist - 0.66 * (master.lastDist - master.v)
		phase = Phase.MIDDLE
		master.y = -1.0
	}

}