package org.aresclient.ares.impl.gui.impl.game.setting

/*
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

class ListSubElement(element: ListElementAdapter<*>, added: Boolean, content: SettingsContent, defaultHeight: Float): SettingElement(defaultHeight) {
    private val button = ListElementToggleButton(added, element, content, defaultHeight)
    private val name = element.formatted()

    init {
        pushChild(button)
        pushChild(SettingElementButton(this) { button.click() })
    }

    override fun getText(): String = name

    // state is const because on toggle - content.refresh is called, so it creates a new toggle element
    private class ListElementToggleButton(private val state: Boolean, private val element: ListElementAdapter<*>,
                                          private val content: SettingsContent, height: Float): SettingSubToggleButton(height) {
        override fun getState(): Boolean = state

        @Suppress("UNCHECKED_CAST")
        override fun setState(value: Boolean) {
            // TODO:
            /*(content.getSerializable() as Setting<ArrayList<Any>>).also {
                if(!state) it.value.add(element.value!!)
                else it.value.remove(element.value)
            }
            content.refresh()*/
        }
    }
}
 */
