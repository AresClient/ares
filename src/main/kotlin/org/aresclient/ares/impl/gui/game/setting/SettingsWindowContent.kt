package org.aresclient.ares.impl.gui.game.setting

import org.aresclient.ares.Ares
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.setting.settings.*
import org.aresclient.ares.api.setting.settings.grouped.GroupedSetting
import org.aresclient.ares.api.setting.settings.list.MapListSetting
import org.aresclient.ares.api.setting.settings.number.DoubleSetting
import org.aresclient.ares.api.setting.settings.number.FloatSetting
import org.aresclient.ares.api.setting.settings.number.IntegerSetting
import org.aresclient.ares.api.setting.settings.number.LongSetting
import org.aresclient.ares.impl.gui.game.setting.settings.*
import org.aresclient.ares.impl.gui.game.setting.settings.number.DoubleElement
import org.aresclient.ares.impl.gui.game.setting.settings.number.FloatElement
import org.aresclient.ares.impl.gui.game.setting.settings.number.IntElement
import org.aresclient.ares.impl.gui.game.setting.settings.number.LongElement
import org.aresclient.ares.impl.gui.game.window.WindowContent

class SettingsWindowContent(settings: MapSetting): WindowContent(settings) {
    private val name = settings.addString("setting", "")
    private val setting = Ares.getSettings().find(name.value)
    private val group = RowSettingGroup(setting, 1, this, width = this::getWidth)
    private var icon = DEFAULT_ICON

    init {
        // set icon if category
        for(category in Module.Category.getAll()) {
            if(category.settings == setting) {
                icon = category.icon
                break
            }
        }

        // TODO: RESIZING WINDOWS?
        pushChild(group)
    }

    override fun getTitle() = setting.getName() ?: "Home"

    override fun getIcon() = icon

    override fun getHeight() = group.getHeight()

    fun createSettingElement(setting: Setting<*>, settingHeight: Float = 18f): RowElement = when(setting.type) {
        Setting.Type.BOOLEAN -> BooleanElement(setting as BooleanSetting, settingHeight)
        Setting.Type.ENUM -> EnumElement(setting as EnumSetting<*>, settingHeight)
        Setting.Type.BIND -> BindElement(setting as BindSetting, settingHeight)
        Setting.Type.STRING -> StringElement(setting as StringSetting, settingHeight)
        Setting.Type.INTEGER -> IntElement(setting as IntegerSetting, settingHeight)
        Setting.Type.LONG -> LongElement(setting as LongSetting, settingHeight)
        Setting.Type.FLOAT -> FloatElement(setting as FloatSetting, settingHeight)
        Setting.Type.DOUBLE -> DoubleElement(setting as DoubleSetting, settingHeight)
        Setting.Type.COLOR -> ColorElement(setting as ColorSetting, settingHeight)
        Setting.Type.MAP -> MapElement(this, setting as MapSetting, settingHeight)
        Setting.Type.MAP_LIST -> MapListElement(this, setting as MapListSetting, settingHeight)
        // TODO: STRING_LIST, ENUM_LIST
        Setting.Type.GROUPED -> GroupedElement(this, setting as GroupedSetting<*, *>, settingHeight)
        else -> RowSettingElement(setting, settingHeight)
    }
}