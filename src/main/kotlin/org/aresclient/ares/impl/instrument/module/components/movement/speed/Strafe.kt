package org.aresclient.ares.impl.instrument.module.components.movement.speed

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.events.TickEvent
import org.aresclient.ares.api.instruments.Component
import org.aresclient.ares.impl.instrument.module.modules.movement.Speed
import org.aresclient.ares.impl.util.EntityUtil.withTransverseMovement
import kotlin.math.sqrt

object Strafe: Component.Settings<Speed>(Speed, "Strafe"), Wrapper {

    init {
        settings.setDescription("Settings for Strafe mode")
        settings.setVisibility { Speed.mode.value == Speed.Mode.STRAFE }
    }

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

    @field:EventHandler
    val motionTickListener = EventListener<TickEvent.Motion> {
        if(master.mode.value != Speed.Mode.STRAFE || MC.NULL) return@EventListener

        val input = SELF.input?.playerInput ?: return@EventListener
        if(!input.forward && !input.backward && !input.left && !input.right) return@EventListener

        if(sprint.value) SELF.setSprinting(true)
        if(SELF.isOnGround) {
            if(low_hop.value) SELF.addVelocity(0.0, height.value, 0.0)
            return@EventListener
        }

        val speed =
            if(!modify_speed.value) sqrt(SELF.velocity.x * SELF.velocity.x + SELF.velocity.z * SELF.velocity.z)
            else speed.value

        SELF.withTransverseMovement(speed)
    }

}