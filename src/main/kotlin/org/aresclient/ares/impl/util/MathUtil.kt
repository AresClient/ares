package org.aresclient.ares.impl.util

import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.aresclient.ares.api.events.PlayerEvent
import org.aresclient.ares.mixin.accessors.AccessMath
import org.joml.Vector2d
import kotlin.math.cos
import kotlin.math.sin

object MathUtil {
	fun Float.getAngleDifference(otherAngle: Float): Float {
		var a = this - otherAngle
		if (a > 180) a -= 360
		else if (a < -180) a += 360
		return -a
	}

	fun Float.lerp(tickProgress: Float, previous: Float): Float {
		return if (tickProgress == 1F) this
		else MathHelper.lerp(tickProgress, this, previous)
	}

	fun Float.lerpAngleDegrees(tickProgress: Float, previous: Float): Float {
		return if (tickProgress == 1F) this
		else MathHelper.lerpAngleDegrees(tickProgress, this, previous)
	}

	fun Float.normalizeAngle(): Float {
		var a = this % 360
		if (a >= 180) a -= 360
		if (a < -180) a += 360
		return a
	}

	fun Vec2f.normalizeRotation(): Vec2f {
		_x = x.normalizeAngle()
		_y = y.normalizeAngle()
		return this
	}

	fun Vec2f.moveCameraWithCursor(event: PlayerEvent.ChangeLookDirection): Vec2f = moveCameraWithCursor(event.cursorDeltaX, event.cursorDeltaY)
	fun Vec2f.moveCameraWithCursor(x: Double, y: Double): Vec2f = moveCameraWithCursor(x.toFloat(), y.toFloat())
	fun Vec2f.moveCameraWithCursor(x: Float, y: Float): Vec2f {
		_x += 0.15F * x
		_y += 0.15F * y
		return this
	}

	fun Vec2f.duplicate(): Vec2f = Vec2f(this.x, this.y)
	fun Vec3d.duplicate(): Vec3d = Vec3d(this.x, this.y, this.z)

	// ──────────────────────────────────────────────────────────────────────── //

	// Was creating a buttload of Vectors so it's probably better to access the values and
	// modify them to reduce object creation overhead when just pushing around numbers.
	// Also probably reduces GC. Just don't use this to set MC values.

	fun Vec2f.set(vector: Vec2f): Vec2f = (this as AccessMath.Vec2f).let {
		setX(vector.x); setY(vector.y)
		return this
	}

	fun Vec2f.set(x: Float = this.x, y: Float = this.y): Vec2f = (this as AccessMath.Vec2f).let {
		setX(x); setY(y)
		return this
	}

	var Vec2f._x: Float get() = this.x
		set(value) = (this as AccessMath.Vec2f).setX(value)

	var Vec2f._y: Float get() = this.y
		set(value) = (this as AccessMath.Vec2f).setY(value)

	fun Vec3d.set(vector: Vec3d): Vec3d = (this as AccessMath.Vec3d).let {
		setX(vector.x); setY(vector.y); setZ(vector.z)
		return this
	}

	fun Vec3d.set(x: Double = this.x, y: Double = this.y, z: Double = this.z): Vec3d = (this as AccessMath.Vec3d).let {
		setX(x); setY(y); setZ(z)
		return this
	}

	var Vec3d._x: Double get() = this.x
		set(value) = (this as AccessMath.Vec3d).setX(value)

	var Vec3d._y: Double get() = this.y
		set(value) = (this as AccessMath.Vec3d).setY(value)

	var Vec3d._z: Double get() = this.z
		set(value) = (this as AccessMath.Vec3d).setZ(value)

	// ──────────────────────────────────────────────────────────────────────── //

	private const val PI_2 = Math.PI / 2
	private const val PI_4 = Math.PI / 4

	/**
	 * @receiver yaw in degrees
	 */
	fun Float.toTransverseMovement(speed: Double, forwards: Float, sideways: Float): Vector2d {
		return Math.toRadians(this.toDouble()).toTransverseMovement(speed, forwards, sideways)
	}

	/**
	 * @receiver yaw in radians
	 */
	fun Double.toTransverseMovement(speed: Double, forwards: Float, sideways: Float): Vector2d {
		var yaw = this; var forwards = forwards; var sideways = sideways

		if (forwards != 0F) {
			if (sideways > 0) yaw += if (forwards > 0) -PI_4 else PI_4
			else if (sideways < 0) yaw += if (forwards > 0) PI_4 else -PI_4

			sideways = 0F

			if (forwards > 0) forwards = 1F
			else if (forwards < 0) forwards = -1F
		}

		yaw += PI_2

		return Vector2d(
			forwards * speed * cos(yaw) + sideways * speed * sin(yaw),
			forwards * speed * sin(yaw) - sideways * speed * cos(yaw)
		)
	}
}