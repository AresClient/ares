package org.aresclient.ares.impl.gui.game.setting.settings

import org.aresclient.ares.Ares
import org.aresclient.ares.api.gui.Button
import org.aresclient.ares.api.gui.DynamicElement
import org.aresclient.ares.api.gui.DynamicElementGroup
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.setting.settings.ColorSetting
import org.aresclient.ares.api.setting.settings.grouped.Group
import org.aresclient.ares.api.setting.settings.grouped.GroupedSetting
import org.aresclient.ares.api.setting.settings.grouped.IGroupMember
import org.aresclient.ares.api.setting.settings.list.MapListSetting
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.gui.game.setting.DropdownSettingContainer
import org.aresclient.ares.impl.gui.game.setting.RowSettingGroup
import org.aresclient.ares.impl.gui.game.setting.SettingsWindowContent
import org.aresclient.ares.impl.gui.game.window.WindowContent
import org.aresclient.ares.impl.util.RenderHelper
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme

class GroupedElement<T, V: Group<T>>(private val content: SettingsWindowContent, setting: GroupedSetting<T, V>, private val scale: Float):
    DropdownSettingContainer<GroupedSetting<T, V>, List<V>>(setting, scale) {
    init {
        pushChild(RowButton(this) {
            content.getWindow()?.open {
                addString("setting", setting.path)
                SettingsWindowContent::class.java
            }
        })
        change()
    }

    override fun change() {
        setDropdown(RowSettingGroup(setting, 1, content, scale * 0.87f))
    }

    class GroupElement<T>(content: SettingsWindowContent, private val group: Group<T>, scale: Float): DropdownSettingContainer<Group<T>, Map<String, Setting<*>>>(group, scale) {
        init {
            pushChild(RowButton(this) {
                group.enabled.value = !group.enabled.value
            })
            pushChild(SubDeleteButton(scale) {
                (group.parent as GroupedSetting<*, *>).remove(group)
            })
            setDropdown(RowSettingGroup(group, 1, content, scale * 0.87f))
        }

        override fun getText(): String = group.title.value
        override fun getTextColor(theme: Theme): Color = if(group.enabled.value) theme.primary.value else theme.lightground.value
    }

    open class ActionButton(private val text: String, action: (Button) -> Unit, scale: Float): DynamicElement(height = { scale }) {
        private val fontRenderer = RenderHelper.getFontRenderer(scale * 13f/18f)

        init {
            pushChild(RowButton(this, action))
        }

        override fun getWidth() = getParent()?.getWidth() ?: 0f

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
            // outline
            val width = getWidth()
            val height = getHeight()
            buffers.lines.draw(matrixStack) {
                vertices(
                    0f, height, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    width, height, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    0f, 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha,
                    width, 0f, 0f, 2f, theme.primary.value.red, theme.primary.value.green, theme.primary.value.blue, theme.primary.value.alpha
                )
                indices(
                    0, 1,
                    0, 2,
                    1, 3
                )
            }

            val color = theme.lightground.value
            fontRenderer.drawString(matrixStack, text, 3f, 1f, color.red, color.green, color.blue, color.alpha)
        }
    }

    class AddGroupElement<T, V: Group<T>>(setting: GroupedSetting<T, V>, scale: Float): ActionButton("Add Group", { setting.add(setting.groupSupplier.get()) }, scale)

    class EditMembersElement<T, V: Group<T>>(content: WindowContent, group: V, scale: Float): ActionButton("Edit Members", {
        content.getWindow()?.open {
            addString("setting", group.path)
            EditMembersContent::class.java
        }
    }, scale)

    class EditMembersContent(settings: MapSetting): WindowContent(settings) {
        private val name = settings.addString("setting", "") // name of Group<T>
        private val setting = Ares.SETTINGS.find(name.value) as? Group<*>

        override fun getTitle() = setting?.title?.value ?: "<null>"
    }


    // TODO:
    class PossibleMembersRowGroup<T>(private val members: Set<IGroupMember<T>>, scale: Float): DynamicElementGroup(1) {
    }
}
