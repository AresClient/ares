package org.aresclient.ares.api.events

import net.minecraft.client.render.RenderTickCounter

abstract class InputEvent(val type: Type): AresEvent("input-" + type.name) {
	enum class Type { KEYBOARD, MOUSE }

	abstract class Keyboard(val state: State, val key: Int): InputEvent(Type.KEYBOARD) {
		enum class State { PRESSED, RELEASED }

		class Pressed(key: Int): Keyboard(State.PRESSED, key)
		class Released(key: Int): Keyboard(State.RELEASED, key)
	}

	abstract class Mouse(val state: State): InputEvent(Type.MOUSE) {
		enum class State { PRESSED, RELEASED, SCROLLED, MOVED }

		class Pressed(val key: Int): Mouse(State.PRESSED)
		class Released(val key: Int): Mouse(State.RELEASED)
		class Scrolled(val vertical: Double): Mouse(State.SCROLLED)
//		class Moved(val x: Double, val y: Double, val dX: Double, val dY: Double, key: Int): Mouse(State.MOVED, key)
	}
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
