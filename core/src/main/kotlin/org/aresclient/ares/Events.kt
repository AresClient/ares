package org.aresclient.ares

import dev.tigr.simpleevents.event.Event

class ScreenOpenedEvent(val mainMenu: Boolean): Event()

abstract class InputEvent(val type: Type): Event() {
    enum class Type {
        KEYBOARD,
        MOUSE
    }

    open class Keyboard(val state: State, val key: Int): InputEvent(Type.KEYBOARD) {
        enum class State {
            PRESSED,
            RELEASED
        }

        class Pressed(key: Int): Keyboard(State.PRESSED, key)
        class Released(key: Int): Keyboard(State.RELEASED, key)
    }

    open class Mouse(val state: State): InputEvent(Type.MOUSE) {
        enum class State {
            PRESSED,
            RELEASED,
            SCROLLED,
            MOVED
        }

        class Pressed(val key: Int): Mouse(State.PRESSED)
        class Released(val key: Int): Mouse(State.RELEASED)
        class Scrolled(val vertical: Double): Mouse(State.SCROLLED)
        class Moved(val x: Double, val y: Double, val deltaX: Double, val deltaY: Double): Mouse(State.MOVED)
    }
}

abstract class PacketEvent(val packet: Any?, val era: Era): Event() {
    enum class Era {
        BEFORE,
        AFTER
    }

    class Sent(packet: Any?, era: Era): PacketEvent(packet, era)
    class Received(packet: Any?, era: Era): PacketEvent(packet, era)
}