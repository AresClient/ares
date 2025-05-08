package org.aresclient.ares.impl.gui.game.setting

import org.aresclient.ares.api.setting.Setting
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

// see also: RowSettingElement
open class DropdownSettingContainer<T: Setting<V>, V>(val setting: T, scale: Float): DropdownContainer(scale) {
    private val listener = Consumer<V> { change() }

    init {
        setVisible { setting.isVisible }
        setting.addListener(listener)
    }

    override fun close() {
        setting.removeListener(listener)
        super.close()
    }

    override fun getText(): String = setting.name ?: "<null>"

    open fun change() {
    }

    override fun click(mouseX: Double, mouseY: Double, mouseButton: Int, acted: AtomicBoolean) {
        super.click(mouseX, mouseY, mouseButton, acted)

        if(mouseButton == 2 && !acted.get() && isMouseOver(mouseX, mouseY)) {
            setting.setDefault()
            acted.set(true)
        }
    }
}
