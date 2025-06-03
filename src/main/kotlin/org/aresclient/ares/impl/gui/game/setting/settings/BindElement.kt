package org.aresclient.ares.impl.gui.game.setting.settings

import dev.tigr.simpleevents.listener.EventListener
import org.aresclient.ares.api.events.InputEvent
import org.aresclient.ares.api.setting.settings.BindSetting
import org.aresclient.ares.api.util.StringUtils.formatToPretty
import org.aresclient.ares.impl.gui.game.setting.RowSettingElement
import org.lwjgl.glfw.GLFW
import java.util.concurrent.atomic.AtomicBoolean

class BindElement(setting: BindSetting, height: Float): RowSettingElement<BindSetting, Int>(setting, height) {
    private var listening = false
    private var text = if(setting.value == GLFW.GLFW_KEY_UNKNOWN) "None" else GLFW.glfwGetKeyName(setting.value, 0).formatToPretty()

    init {
        pushChild(RowButton(this) { listen(true) })
    }

    override fun change() {
        text = if(setting.value == GLFW.GLFW_KEY_UNKNOWN) "None" else GLFW.glfwGetKeyName(setting.value, 0).formatToPretty()
    }

    override fun getSecondaryText() = if(listening) "..." else text

    override fun click(mouseX: Double, mouseY: Double, mouseButton: Int, acted: AtomicBoolean) {
        if(mouseButton == 1 && !acted.get() && isMouseOver(mouseX, mouseY)) {
            setting.value = GLFW.GLFW_KEY_UNKNOWN
            listen(false)
            acted.set(true)
        }

        super.click(mouseX, mouseY, mouseButton, acted)
    }

    override fun close() {
        listen(false)
        super.close()
    }

    private val onInputEvent: EventListener<InputEvent> = EventListener<InputEvent> { event ->
        if(event.type == InputEvent.Type.KEYBOARD) {
            event as InputEvent.Keyboard
            if(event.state == InputEvent.Keyboard.State.RELEASED) {
                if(event.key != GLFW.GLFW_KEY_ESCAPE) setting.value = event.key
                listen(false)
            }
        } else if (event.type == InputEvent.Type.MOUSE) {
            event as InputEvent.Mouse
            if(event.state == InputEvent.Mouse.State.RELEASED) {
                event as InputEvent.Mouse.Released
                if(event.key != GLFW.GLFW_MOUSE_BUTTON_LEFT && event.key != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    setting.value = event.key
                    listen(false)
                }
            }
        }
    }

    private fun listen(state: Boolean) {
        if(state) EVENTS.register(onInputEvent)
        else EVENTS.unregister(onInputEvent)
        listening = state
    }
}
