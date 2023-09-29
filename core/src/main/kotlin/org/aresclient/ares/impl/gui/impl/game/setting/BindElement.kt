package org.aresclient.ares.impl.gui.impl.game.setting

import dev.tigr.simpleevents.listener.EventListener
import org.aresclient.ares.api.Ares
import org.aresclient.ares.api.event.client.InputEvent
import org.aresclient.ares.impl.gui.impl.game.SettingElement
import org.aresclient.ares.impl.util.Theme
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.util.Keys
import java.util.concurrent.atomic.AtomicBoolean

class BindElement(setting: Setting.Bind, height: Float): SettingElement<Setting.Bind>(setting, height) {
    private var listening = false
    private var name = name()

    init {
        pushChild(SettingElementButton(this) { listen(true) })
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val text = if(listening) "..." else name
        fontRenderer.drawString(
            matrixStack, text, getWidth() - fontRenderer.getStringWidth(text) - 2, 1f,
            theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
        )

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        if(mouseButton == 1 && !acted.get() && isMouseOver(mouseX, mouseY)) {
            set(Keys.UNKNOWN)
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
            if(event.state == InputEvent.Keyboard.State.PRESSED) {
                if(event.key == Keys.ESCAPE) listen(false)
                else set(event.key)
            }
        } else if (event.type == InputEvent.Type.MOUSE) {
            event as InputEvent.Mouse
            if(event.state == InputEvent.Mouse.State.PRESSED) {
                event as InputEvent.Mouse.Pressed
                if(event.key != Keys.MOUSE_LEFT && event.key != Keys.MOUSE_RIGHT)
                    set(event.key)
            }
        }
    }

    private fun set(key: Int) {
        setting.value = key
        name = name()
        listen(false)
    }

    private fun listen(state: Boolean) {
        if(state) Ares.getEventManager().register(onInputEvent)
        else Ares.getEventManager().unregister(onInputEvent)
        listening = state
    }

    private fun name(): String = if(setting.value == Keys.UNKNOWN) "None" else Keys.getName(setting.value).formatToPretty()
}