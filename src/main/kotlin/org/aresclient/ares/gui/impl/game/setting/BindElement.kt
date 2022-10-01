package org.aresclient.ares.gui.impl.game.setting

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import net.meshmc.mesh.event.events.client.InputEvent
import net.meshmc.mesh.util.Keys
import org.aresclient.ares.Ares
import org.aresclient.ares.Setting
import org.aresclient.ares.gui.impl.game.SettingElement
import org.aresclient.ares.renderer.MatrixStack
import org.aresclient.ares.utils.Renderer
import org.aresclient.ares.utils.Theme
import java.util.concurrent.atomic.AtomicBoolean

class BindElement(private val setting: Setting<Int>): SettingElement({
    (it as BindElement).listen(true)
}) {
    private var listening = false
    private var name = name()

    override fun getText(): String = setting.getName()

    override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val text = if(listening) "..." else name
        fontRenderer.drawString(
            matrixStack, text, getWidth() - fontRenderer.getStringWidth(text) - 2, 1f,
            theme.lightground.red, theme.lightground.green, theme.lightground.blue, theme.lightground.alpha
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

    @field:EventHandler
    private val onInputEvent = EventListener<InputEvent> { event ->
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
        if(state) Ares.MESH.eventManager.register(onInputEvent)
        else Ares.MESH.eventManager.unregister(onInputEvent)
        listening = state
    }

    private fun name(): String = if(setting.value == Keys.UNKNOWN) "None" else Keys.getName(setting.value).formatToPretty()
}