package org.aresclient.ares.gui.impl.game.setting

import org.aresclient.ares.Setting
import org.aresclient.ares.gui.impl.game.SettingElement
import org.aresclient.ares.gui.impl.game.SettingSubToggleButton
import org.aresclient.ares.gui.impl.game.SettingsContent

abstract class ListElementAdapter<T>(val value: T) {
    abstract fun formatted(): String
}

class EnumListElementAdapter<T: Enum<*>>(value: T): ListElementAdapter<T>(value) {
    override fun formatted(): String =
        value.name.split('_').joinToString(separator = " ") { it.lowercase().replaceFirstChar { c -> c.uppercase() }}
}

class DefaultListElementAdapter<T>(value: T): ListElementAdapter<T>(value) {
    override fun formatted(): String = value.toString()
}

class ListSubElement(element: ListElementAdapter<*>, added: Boolean, content: SettingsContent): SettingElement(element.formatted(), {
    (it as ListSubElement).button.click()
}) {
    private val button = ListElementToggleButton(added, element, content)

    init {
        pushChild(button)
    }

    // state is const because on toggle - content.refresh is called, so it creates a new toggle element
    private class ListElementToggleButton(private val state: Boolean, private val element: ListElementAdapter<*>,
                                          private val content: SettingsContent): SettingSubToggleButton() {
        override fun getState(): Boolean = state

        @Suppress("UNCHECKED_CAST")
        override fun setState(value: Boolean) {
            (content.getSerializable() as Setting<ArrayList<Any>>).also {
                if(!state) it.value.add(element.value!!)
                else it.value.remove(element.value)
            }
            content.refresh()
        }
    }
}
