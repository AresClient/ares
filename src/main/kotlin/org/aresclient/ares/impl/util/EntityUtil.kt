package org.aresclient.ares.impl.util

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.Monster
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec2f
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.instrument.global.Rotation
import org.aresclient.ares.impl.util.EntityUtil.isTarget
import org.aresclient.ares.impl.util.MathUtil.toTransverseMovement
import org.joml.Vector2d

object EntityUtil: Wrapper {
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

	// TODO: IMPLEMENT FRIENDS SYSTEM
	fun Entity.isFriend(): Boolean = false

	fun Entity.isBot(): Boolean = this is PlayerEntity && isInvisibleTo(MC.player) && !isOnGround && !collidesWith(MC.player)

	enum class TargetType {
		SELF, PLAYER, FRIEND, TEAMMATE, PASSIVE, HOSTILE, ITEM, BOT, OTHER;
	}

	fun Entity.getTargetType(): TargetType {
		if(this == MC.player) return TargetType.SELF
		return when(this) {
			is ItemEntity -> TargetType.ITEM
			is PassiveEntity -> TargetType.PASSIVE
			is Monster -> TargetType.HOSTILE
			is PlayerEntity -> {
				return if(isFriend()) TargetType.FRIEND
				else if(isBot()) TargetType.BOT
				else if(scoreboardTeam != null && scoreboardTeam == MC.player?.scoreboardTeam) TargetType.TEAMMATE
				else TargetType.PLAYER
			}
			else -> TargetType.OTHER
		}
	}

	fun Entity.getTargetColor(): Color {
		return when(getTargetType()) {
			TargetType.SELF -> Color.WHITE
			TargetType.PLAYER -> Color.BLUE
			TargetType.FRIEND -> Color.rainbow()
			TargetType.TEAMMATE -> Color.rainbow()
			TargetType.PASSIVE -> Color.GREEN
			TargetType.HOSTILE -> Color.RED
			TargetType.ITEM -> Color.WHITE
			TargetType.BOT -> Color.BLACK
			TargetType.OTHER -> Color.WHITE
		}
	}

	fun Entity.isTarget(players: Boolean, friends: Boolean, teammates: Boolean, passive: Boolean, hostile: Boolean, items: Boolean, nametagged: Boolean, bots: Boolean): Boolean {
		if(this == MC.player) return false
		if(hasCustomName() && nametagged) return true
		return when(this) {
			is ItemEntity -> items
			is PassiveEntity -> passive
			is Monster -> hostile
			is PlayerEntity -> {
				return if(players) {
					if(isFriend()) friends
					else if(isBot()) bots
					else if(scoreboardTeam != null && scoreboardTeam == MC.player?.scoreboardTeam) teammates
					else true
				} else false
			}
			else -> false
		}
	}

	fun ClientPlayerEntity.getTransverseMovement(speed: Double, cameraRotation: Boolean = true): Vector2d {
		val yaw = if (cameraRotation && Rotation.isRotating) {
			if (MC.options.perspective.isFrontView) MC.gameRenderer.camera.yaw - 180F
			else MC.gameRenderer.camera.yaw
		} else this.yaw
		return yaw.toTransverseMovement(speed, forwardSpeed, sidewaysSpeed)
	}

	fun ClientPlayerEntity.withTransverseMovement(speed: Double, cameraRotation: Boolean = true, y: Double = this.velocity.y) {
		val movement = getTransverseMovement(speed, cameraRotation)
		this.setVelocity(movement.x, y, movement.y)
	}
}