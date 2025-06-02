package org.aresclient.ares.impl.util

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.SpawnGroup.*
import net.minecraft.entity.decoration.EndCrystalEntity
import net.minecraft.entity.mob.Monster
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.Registries
import net.minecraft.util.math.Vec2f
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.setting.settings.grouped.GroupMember
import org.aresclient.ares.api.setting.settings.grouped.GroupMembers
import org.aresclient.ares.api.setting.settings.grouped.IGroupMember
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.api.util.StringUtils.formatToPretty
import org.aresclient.ares.impl.instrument.globals.Rotation
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

	interface Target {
		val defaultColor: Color
		val defaultRainbow: Boolean
	}

	enum class PlayerThreat(override val defaultColor: Color, override val defaultRainbow: Boolean = false): Target {
		SELF(Color.WHITE),
		FRIEND(Color.CYAN, true),
		HOSTILE(Color.RED),
		BOT(Color.BLACK)
	}

	object EntityTypes {
		val player = PlayerThreat.entries.toSet()
		val monster = HashSet<EntityType<*>>()
		val animal = HashSet<EntityType<*>>()
		val miscellaneous = HashSet<EntityType<*>>()

		val possibles: Set<IGroupMember<Any>>

		init {
			Registries.ENTITY_TYPE.forEach {
				when(it.spawnGroup) {
					MONSTER -> monster.add(it)
					CREATURE, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS, AMBIENT, WATER_AMBIENT -> animal.add(it)
					MISC -> if(it != EntityType.PLAYER) miscellaneous.add(it)
					else -> Unit
				}
			}

			possibles = setOf(
				GroupMembers("Players", player.map { GroupMember("ares:player_${it.name.lowercase()}", it.name.formatToPretty(), it) }),
				GroupMembers("Monsters", monster.toGroupMembers()),
				GroupMembers("Animals", animal.toGroupMembers()),
				GroupMembers("Miscellaneous", miscellaneous.toGroupMembers()),
			)
		}

		private fun HashSet<EntityType<*>>.toGroupMembers(): List<GroupMember<Any>> = map { type ->
			GroupMember(EntityType.getId(type).toString(), type.name.string, type)
		}
	}

	fun Entity.isFriend(): Boolean = this is PlayerEntity && FriendUtil.isFriend(this.gameProfile)

	fun Entity.isBot(): Boolean = this is PlayerEntity && isInvisibleTo(MC.player) && !isOnGround && !collidesWith(MC.player)

	enum class TargetType(override val defaultColor: Color, override val defaultRainbow: Boolean = false): Target {
		PASSIVE(Color.GREEN),
		HOSTILE(Color.BLUE),
		ITEM(Color.WHITE),
		END_CRYSTAL(Color.MAGENTA),
		OTHER(Color.GRAY);
	}

	val Entity.targetType: Target get() {
		return when(this) {
			is EndCrystalEntity -> TargetType.END_CRYSTAL
			is ItemEntity -> TargetType.ITEM
			is PassiveEntity -> TargetType.PASSIVE
			is Monster -> TargetType.HOSTILE
			is PlayerEntity -> playerThreat
			else -> TargetType.OTHER
		}
	}

	val PlayerEntity.playerThreat: PlayerThreat get() {
		return if(this == SELF) PlayerThreat.SELF
		else if(isFriend()) PlayerThreat.FRIEND
		else if(isBot()) PlayerThreat.BOT
		else PlayerThreat.HOSTILE
	}

	fun Entity.isTarget(players: Boolean, friends: Boolean, teammates: Boolean, passive: Boolean, hostile: Boolean, items: Boolean, nametagged: Boolean, bots: Boolean): Boolean {
		if(this == SELF) return false
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