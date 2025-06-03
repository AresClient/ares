package org.aresclient.ares.api.events

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.MovementType
import net.minecraft.network.packet.Packet
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.chunk.WorldChunk
import org.aresclient.ares.api.instruments.Module

abstract class CameraEvent(val delta: Float, type: String): AresEvent("camera-$type") {
	class Position(delta: Float, var x: Double, var y: Double, var z: Double): CameraEvent(delta, "position")
	class Rotation(delta: Float, var yaw: Float, var pitch: Float): CameraEvent(delta, "rotation")
}

abstract class InputEvent(val type: Type): AresEvent("input-" + type.name) {
	enum class Type { KEYBOARD, MOUSE }

	var textboxFocused = false

	abstract class Keyboard(val state: State, val key: Int): InputEvent(Type.KEYBOARD) {
		enum class State { PRESSED, RELEASED }

		class Pressed(key: Int, val repeat: Boolean): Keyboard(State.PRESSED, key)
		class Released(key: Int): Keyboard(State.RELEASED, key)
	}

	abstract class Mouse(val state: State): InputEvent(Type.MOUSE) {
		enum class State { PRESSED, RELEASED, SCROLLED, MOVED }

		class Pressed(val key: Int, val repeat: Boolean): Mouse(State.PRESSED)
		class Released(val key: Int): Mouse(State.RELEASED)
		class Scrolled(val vertical: Double): Mouse(State.SCROLLED)
//		class Moved(val x: Double, val y: Double, val dX: Double, val dY: Double, key: Int): Mouse(State.MOVED, key)
	}
}

class CharTypedEvent(val codePoint: Int, val modifers: Int): AresEvent("char-typed")

abstract class PlayerEvent(name: String): AresEvent("player-$name") {
	class ChangeLookDirection(val cursorDeltaX: Double, val cursorDeltaY: Double): PlayerEvent("change-look-direction")
	class Move(val movementType: MovementType, val movement: Vec3d): PlayerEvent("move")
	class UpdateVelocityYaw(var yaw: Float): PlayerEvent("update-velocity-yaw")
}

abstract class RenderEvent(val type: Type): AresEvent("render-" + type.name) {
	//TODO: ERA?
	enum class Type { HUD, WORLD }

	class Hud(val tickDelta: Float): RenderEvent(Type.HUD)
	class World(val tickDelta: Float): RenderEvent(Type.WORLD)
}

class ScreenOpenedEvent(val mainMenu: Boolean): AresEvent("screen-open")
class ShutdownEvent: AresEvent("shutdown")

abstract class TickEvent(val type: Type, era: Era): AresEvent("tick-" + type.name, era) {
	enum class Type { CLIENT, GAMELOOP, WORLD, MOTION }

	class Client(era: Era): TickEvent(Type.CLIENT, era)
	class GameLoop(era: Era): TickEvent(Type.GAMELOOP, era)
	class World(era: Era): TickEvent(Type.WORLD, era)
	class Motion(era: Era): TickEvent(Type.MOTION, era)
}

class ChatEvent(val message: String): AresEvent("chat")

class ToggleEvent(val module: Module, val enabled: Boolean)

open class PacketEvent(val type: Type, val packet: Packet<*>, era: Era): AresEvent("packet-${type.name}", era) {
	enum class Type { RECEIVE, SEND }

	class Receive(packet: Packet<*>, era: Era): PacketEvent(Type.RECEIVE, packet, era)
	class Send(packet: Packet<*>, era: Era): PacketEvent(Type.SEND, packet, era)
}

class RenderEntityLabelEvent(val text: Text): AresEvent("render-entity-label")

class BlockStateUpdateEvent(val pos: BlockPos, val newState: BlockState?, val oldState: BlockState?): AresEvent("block-state-update")

class LoadChunkEvent(val chunk: WorldChunk): AresEvent("load-chunk")

open class BlockEntityEvent(name: String, val blockEntity: BlockEntity?): AresEvent("load-block-entity") {
	class Add(blockEntity: BlockEntity?): BlockEntityEvent("add", blockEntity)
	class Remove(blockEntity: BlockEntity?): BlockEntityEvent("remove", blockEntity)
}

class KeyboardInputEvent(era: Era): AresEvent("keyboard-input-tick", era)

class BlockOcclusionEvent: AresEvent("block-occlusion")
