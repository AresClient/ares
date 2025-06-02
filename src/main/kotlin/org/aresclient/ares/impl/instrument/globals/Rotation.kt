package org.aresclient.ares.impl.instrument.globals

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.events.Era
import org.aresclient.ares.api.events.KeyboardInputEvent
import org.aresclient.ares.api.events.PlayerEvent
import org.aresclient.ares.api.instruments.Global
import org.aresclient.ares.api.instruments.Prioritizer
import org.aresclient.ares.impl.util.EntityUtil.rotation
import org.aresclient.ares.impl.util.MathUtil._x
import org.aresclient.ares.impl.util.MathUtil._y
import org.aresclient.ares.impl.util.MathUtil.duplicate
import org.aresclient.ares.impl.util.MathUtil.getAngleDifference
import org.aresclient.ares.impl.util.MathUtil.moveCameraWithCursor
import org.aresclient.ares.impl.util.MathUtil.normalizeRotation
import org.aresclient.ares.impl.util.MathUtil.set
import org.aresclient.ares.impl.util.Timer
import kotlin.math.min

interface Rotator: Prioritizer {
    val yawStep: Float get() = Rotation.yaw_step.value
    val pitchStep: Float get() = Rotation.pitch_step.value
    val rotation: Vec2f
}

object Rotation: Global.PriorityHandler<Rotator>("Rotation", "Handles rotation so that the character is facing in the expected direction for an action."), CameraAdjustor {

    private val reset_delay = settings.addLong("Reset Delay", 10)
        .setMin(0)
        .setMax(100)
        .setDescription(
            "How long to wait after completing a rotation before resetting to the same rotation as the camera."
        )

    private val completion_delay = settings.addLong("Completion Delay", 1)
        .setMin(0)
        .setMax(10)
        .setDescription("How long to wait after completing a rotation before interactions can happen.")

    private val grouping_density = settings.addFloat("Grouping Density", 0F)
        .setMin(0F)
        .setMax(180F)
        .setDescription("The rotation distance in degrees within which to ignore the completion delay.")

    internal val yaw_step = settings.addFloat("Yaw Step", 180F)
        .setMin(1F)
        .setMax(180F)
        .setDescription("How many degrees to turn horizontally per tick.")

    internal val pitch_step = settings.addFloat("Pitch Step", 180F)
        .setMin(1F)
        .setMax(180F)
        .setDescription("How many degrees to turn vertically per tick.")

    private val grim_strict = settings.addBoolean("Grim Strict", false)
        .setDescription("Whether to use keyboard inputs to modify movement, or normal movement calculated from camera yaw",)

    // ════════════════════════════════════════════════════════════════════════ //

    override fun begin() {
        cameraRotation!!.set(MC.gameRenderer.camera.yaw, MC.gameRenderer.camera.pitch)
        lastRotation.set(MC.player?.yaw ?: 0F, MC.player?.pitch ?: 0F)
    }

    override fun end() {
        MC.player!!.rotation = cameraRotation
        Camera.end(this)
    }

    override val cameraPosition: Vec3d? = null
    override val cameraRotation: Vec2f = Vec2f.ZERO.duplicate()
    override val shouldRenderCharacter: Boolean = false
    override fun priority(): Int = 1

    // ════════════════════════════════════════════════════════════════════════ //

    val currentRotation = Vec2f.ZERO.duplicate()
    val lastRotation = Vec2f.ZERO.duplicate()
    val resetTimer = Timer()
    var steppingComplete = true

    // ════════════════════════════════════════════════════════════════════════ //

    override fun onTick() {
        if(keys.isEmpty() && Camera.isActive(this) && resetTimer.hasTicksPassed(reset_delay.value)) {
            Camera.end(this)
            resetTimer.reset()
            return
        }

        if(MC.world == null || MC.player == null || keys.isEmpty()) {
            resetTimer.reset()
            return
        }

        val current = getCurrent() ?: return
        currentRotation.set(current.rotation).normalizeRotation()
        Camera.begin(this)

        val yawStep = min(current.yawStep, yaw_step.value)
        val pitchStep = min(current.pitchStep, pitch_step.value)

        if(!lastRotation.equals(currentRotation) && yawStep != 180F || pitchStep != 180F) {
            val xChange = lastRotation.x.getAngleDifference(currentRotation.x)
            val yChange = lastRotation.y.getAngleDifference(currentRotation.y)
            currentRotation._x = steppedAngle(xChange, yawStep, lastRotation.x, currentRotation.x)
            currentRotation._y = steppedAngle(yChange, pitchStep, lastRotation.y, currentRotation.y)
        }
        else steppingComplete = true

        MC.player!!.rotation = currentRotation

        lastRotation.set(currentRotation)
    }

    @field:EventHandler private val changeLookDirection = EventListener<PlayerEvent.ChangeLookDirection> { event ->
        if(!isRotating) return@EventListener
        if(Camera.hasPriority(this)) event.isCancelled = true
        cameraRotation.moveCameraWithCursor(event)
    }

    @field:EventHandler private val updateVelocityYaw = EventListener<PlayerEvent.UpdateVelocityYaw> { event ->
        if(!Camera.hasPriority(this) || grim_strict.value) return@EventListener

        event.isCancelled = true
        event.yaw = cameraRotation.x
    }

    @field:EventHandler private val onTickKeyboardInput = EventListener<KeyboardInputEvent> { event ->
        if(MC.NULL || !Camera.hasPriority(this) || !grim_strict.value) return@EventListener

        if(event.era == Era.AFTER) {
            resetInputs()
            return@EventListener
        }

        retrieveInputs()
        modifyInputs()
    }

    // ════════════════════════════════════════════════════════════════════════ //

    private fun steppedAngle(change: Float, step: Float, last: Float, current: Float): Float =
        if(change > step) {
            steppingComplete = false
            last + step
        }
        else if(change < -step) {
            steppingComplete = false
            last - step
        }
        else {
            steppingComplete = true
            current
        }

    private val inputs = booleanArrayOf(false, false, false, false)
    private fun resetInputs() {
        val opt = MC.options

        opt.forwardKey.isPressed = false; opt.backKey.isPressed = false;
        opt.leftKey.isPressed = false; opt.rightKey.isPressed = false

        if(inputs[0]) opt.forwardKey.isPressed = true
        if(inputs[1]) opt.backKey.isPressed = true
        if(inputs[2]) opt.leftKey.isPressed = true
        if(inputs[3]) opt.rightKey.isPressed = true

        inputs.fill(false)
    }

    private fun retrieveInputs() {
        val opt = MC.options

        if(opt.forwardKey.isPressed) {
            opt.forwardKey.isPressed = false
            inputs[0] = true
        }

        if(opt.backKey.isPressed) {
            opt.backKey.isPressed = false
            inputs[1] = true
        }

        if(opt.leftKey.isPressed) {
            opt.leftKey.isPressed = false
            inputs[2] = true
        }

        if(opt.rightKey.isPressed) {
            opt.rightKey.isPressed = false
            inputs[3] = true
        }
    }

    private fun modifyInputs() {
        val diff = Camera.getYawDifference()
        val opt = MC.options

        if(diff in -67.5f..67.5f) { // Forward
            if(inputs[0]) opt.forwardKey.isPressed = true
            if(inputs[1]) opt.backKey.isPressed = true
            if(inputs[2]) opt.leftKey.isPressed = true
            if(inputs[3]) opt.rightKey.isPressed = true
        }

        if(diff !in -112.5f..112.5f) { // Backward
            if(inputs[0]) opt.backKey.isPressed = true
            if(inputs[1]) opt.forwardKey.isPressed = true
            if(inputs[2]) opt.rightKey.isPressed = true
            if(inputs[3]) opt.leftKey.isPressed = true
        }

        if(diff in -157.5f..-22.5f) { // Leftward
            if(inputs[0]) opt.rightKey.isPressed = true
            if(inputs[1]) opt.leftKey.isPressed = true
            if(inputs[2]) opt.forwardKey.isPressed = true
            if(inputs[3]) opt.backKey.isPressed = true
        }

        if(diff in 22.5f..157.5f) { // Rightward
            if(inputs[0]) opt.leftKey.isPressed = true
            if(inputs[1]) opt.rightKey.isPressed = true
            if(inputs[2]) opt.backKey.isPressed = true
            if(inputs[3]) opt.forwardKey.isPressed = true
        }
    }

    val isRotating: Boolean get() = Camera.isActive(this)
}

