package org.aresclient.ares.impl.instrument.module.modules.movement

import org.aresclient.ares.api.events.PlayerEvent
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.impl.instrument.module.components.movement.speed.Strafe
import org.aresclient.ares.impl.instrument.module.components.movement.speed.StrafeHop
import org.aresclient.ares.impl.instrument.module.components.movement.speed.YPort
import org.aresclient.ares.impl.util.EntityUtil.getTransverseMovement
import org.aresclient.ares.impl.util.MathUtil._x
import org.aresclient.ares.impl.util.MathUtil._y
import org.aresclient.ares.impl.util.MathUtil._z
import org.aresclient.ares.mixin.accessors.AccessMinecraftClient
import org.aresclient.ares.mixin.accessors.AccessRenderTickCounter
import kotlin.math.hypot
import kotlin.math.max

object Speed: Module(Category.MOVEMENT, "Speed", "") {

    init {
        Strafe
        StrafeHop
        YPort
    }

    enum class Mode {
        STRAFE,
        STRAFE_HOP,
        Y_PORT
    }

    val mode = settings.addEnum("Mode", Mode.STRAFE_HOP)

    var lastMode: Mode? = null
    var lastDist = 0.2873
    var speed = 0.2873
    var v = 0.2873
    var y = 0.0

    override fun onTick() {
        if(MC.NULL) return

        if(mode.value != lastMode) {
            lastMode = mode.value
            StrafeHop.reset()
            YPort.reset()
            speed = 0.0
            lastDist = 0.0
            return
        }

        if(mode.value == Mode.STRAFE) return

        ((MC as AccessMinecraftClient).renderTickCounter as AccessRenderTickCounter).setTickTime(45.99221F)

        lastDist = hypot(SELF.x - SELF.lastX, SELF.z - SELF.lastZ)
    }

    fun PlayerEvent.Move.changeMovement() {
        val dir = SELF.getTransverseMovement(max(v, speed))
        SELF.setVelocity(dir.x, y, dir.y)

        movement._x = SELF.velocity.x
        movement._y = SELF.velocity.y
        movement._z = SELF.velocity.z
        isCancelled = true
    }
}