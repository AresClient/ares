package org.aresclient.ares.impl.gui.game.setting

import org.aresclient.ares.api.gui.DynamicElementGroup
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.api.setting.settings.grouped.Group
import org.aresclient.ares.api.setting.settings.grouped.GroupedSetting
import org.aresclient.ares.api.setting.settings.list.MapListSetting
import org.aresclient.ares.impl.gui.game.setting.settings.ColorElement
import org.aresclient.ares.impl.gui.game.setting.settings.GroupedElement

class RowSettingGroup(
    private val setting: Setting<*>, columns: Int, private val content: SettingsWindowContent, private val settingHeight: Float = 18f,
    visible: () -> Boolean = { true }, x: () -> Float = { 0f }, y: () -> Float = { 0f }, width: () -> Float = { 0f },
    height: () -> Float = { 0f }): DynamicElementGroup(columns, visible, x, y, width, height) {

    init {
        refresh()
    }

    fun refresh() {
        getChildren().clear()
        when(setting.type) {
            Setting.Type.MAP -> {
                val isGroup = setting is Group<*>
                (setting as MapSetting).value.forEach { (name, child) ->
                    if(name.first() != '.' && name != "Enabled" && (name != "Members" || !isGroup))
                        pushChild(content.createSettingElement(child, settingHeight))
                }
                if(isGroup) pushChild(GroupedElement.EditMembersElement(content, setting as Group<*>, settingHeight))
            }
            Setting.Type.COLOR -> pushChild(ColorElement.DropDown(setting as ColorSetting, settingHeight))
            Setting.Type.MAP_LIST -> (setting as MapListSetting).forEach {
                pushChild(content.createSettingElement(it, settingHeight))
            }
            Setting.Type.GROUPED -> {
                val groupedSetting = setting as GroupedSetting<*, *>
                groupedSetting.forEach {
                    pushChild(GroupedElement.GroupElement(content, it, settingHeight))
                }
                pushChild(GroupedElement.AddGroupElement(groupedSetting, settingHeight))
            }
            else -> throw RuntimeException("Can't open setting of type ${setting.type.name} in window")
        }
    }
}
