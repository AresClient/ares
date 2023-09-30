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
    private var text = if(setting.value == Keys.UNKNOWN) "None" else Keys.getName(setting.value).formatToPretty()

    init {
        pushChild(SettingElementButton(this) { listen(true) })
    }

    override fun change() {
        text = if(setting.value == Keys.UNKNOWN) "None" else Keys.getName(setting.value).formatToPretty()
    }

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val text = if(listening) "..." else text
        fontRenderer.drawString(
            matrixStack, text, getWidth() - fontRenderer.getStringWidth(text) - 2, 1f,
            theme.lightground.value.red, theme.lightground.value.green, theme.lightground.value.blue, theme.lightground.value.alpha
        )

        super.draw(theme, buffers, matrixStack, mouseX, mouseY, delta)
    }

    override fun click(mouseX: Int, mouseY: Int, mouseButton: Int, acted: AtomicBoolean) {
        if(mouseButton == 1 && !acted.get() && isMouseOver(mouseX, mouseY)) {
            setting.value = Keys.UNKNOWN
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
            if(event.state == InputEvent.Keyboard.State.PRESSED) {
                if(event.key != Keys.ESCAPE) setting.value = event.key
                listen(false)
            }
        } else if (event.type == InputEvent.Type.MOUSE) {
            event as InputEvent.Mouse
            if(event.state == InputEvent.Mouse.State.PRESSED) {
                event as InputEvent.Mouse.Pressed
                if(event.key != Keys.MOUSE_LEFT && event.key != Keys.MOUSE_RIGHT) {
                    setting.value = event.key
                    listen(false)
                }
            }
        }
    }

    private fun listen(state: Boolean) {
        if(state) Ares.getEventManager().register(onInputEvent)
        else Ares.getEventManager().unregister(onInputEvent)
        listening = state
    }
}