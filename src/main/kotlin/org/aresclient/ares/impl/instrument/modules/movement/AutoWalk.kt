package org.aresclient.ares.impl.instrument.modules.movement

import baritone.api.BaritoneAPI
import baritone.api.pathing.goals.GoalXZ
import net.minecraft.util.math.MathHelper
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.impl.util.BaritoneUtil
import org.aresclient.ares.impl.util.ChatUtil

object AutoWalk: Module(Category.MOVEMENT, "AutoWalk", "Automatically walk in a direction") {
    private const val BORDER: Int = 30000000
    
    enum class Mode {
        SMART, SIMPLE
    }

    private val mode = settings.addEnum("Mode", Mode.SMART)
    private val forceDirection = settings.addBoolean("Force Direction", false)
    private val forcedDirection = settings.addEnum("Direction", Direction.NORTH).setVisibility(forceDirection::getValue)

    private lateinit var direction: Direction
    private var pathing = false

    override fun onEnable() {
        direction = if(forceDirection.value) forcedDirection.value else getDirection()

        if(mode.value == Mode.SMART) startPathing()
    }

    public override fun onMotion() {
        if(mode.value === Mode.SIMPLE) {
            if(pathing) stopPathing()
            SELF.yaw = direction.yaw
            MC.options.forwardKey.isPressed = true
        }

        if(mode.value === Mode.SMART && !pathing) startPathing()
    }

    override fun onDisable() {
        if(mode.value == Mode.SMART) stopPathing()
        else MC.options.forwardKey.isPressed = false
    }

    private fun startPathing() {
        if(!BaritoneUtil.isBaritonePresent()) {
            mode.value = Mode.SIMPLE
            ChatUtil.error("Install Baritone to use AutoWalk smart mode")
            return
        }

        BaritoneAPI.getProvider().primaryBaritone.customGoalProcess.setGoalAndPath(when(direction) {
            Direction.NORTH -> GoalXZ(SELF.x.toInt(), -BORDER)
            Direction.NORTHEAST -> GoalXZ(BORDER, -BORDER)
            Direction.EAST -> GoalXZ(BORDER, SELF.z.toInt())
            Direction.SOUTHEAST -> GoalXZ(BORDER, BORDER)
            Direction.SOUTH -> GoalXZ(SELF.x.toInt(), BORDER)
            Direction.SOUTHWEST -> GoalXZ(-BORDER, BORDER)
            Direction.WEST -> GoalXZ(-BORDER, SELF.z.toInt())
            Direction.NORTHWEST -> GoalXZ(-BORDER, -BORDER)
        })
        pathing = true
    }

    private fun stopPathing() {
        if(!BaritoneUtil.isBaritonePresent()) return
        BaritoneAPI.getProvider().primaryBaritone.pathingBehavior.cancelEverything()
        pathing = false
    }

    private fun getDirection(): Direction {
        val dir = MathHelper.floor((SELF.yaw * 8.0f / 360.0f).toDouble() + 0.5) and 7
        return Direction.entries[dir]
    }

    enum class Direction(val yaw: Float) {
        SOUTH(0f), SOUTHWEST(45f), WEST(90f), NORTHWEST(135f), NORTH(180f), NORTHEAST(225f), EAST(270f), SOUTHEAST(315f)
    }

    override fun getInfo() = mode.value.name
}
