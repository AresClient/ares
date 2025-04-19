package org.aresclient.ares.impl.util

import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec2f

object EntityUtil {
	var Entity.rotation: Vec2f
		get() = Vec2f(yaw, pitch)
		set(value) {
			this.yaw = value.x
			this.pitch = value.y
		}

	fun Entity.setRotation(yaw: Float, pitch: Float) {
		this.yaw = yaw
		this.pitch = pitch
	}
}