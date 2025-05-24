package org.aresclient.ares.impl.gui.game.setting.settings

import org.aresclient.ares.Ares
import org.aresclient.ares.api.gui.Button
import org.aresclient.ares.api.gui.DynamicElement
import org.aresclient.ares.api.gui.DynamicElementGroup
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.setting.settings.grouped.*
import org.aresclient.ares.api.util.Color
import org.aresclient.ares.impl.gui.game.setting.*
import org.aresclient.ares.impl.gui.game.window.WindowContent
import org.aresclient.ares.impl.util.RenderHelper.draw
import org.aresclient.ares.impl.util.Theme

class GroupedElement<T, V: Group<T>>(private val content: SettingsWindowContent, setting: GroupedSetting<T, V>, scale: Float):
    DropdownSettingContainer<GroupedSetting<T, V>, List<V>>(setting, scale) {
    init {
        pushChild(RowButton(this) {
            content.getWindow()?.open {
                addString("setting", setting.path)
                SettingsWindowContent::class.java
            }
        })
        setDropdown(RowSettingGroup(setting, 1, content, scale * 0.87f))
    }

    override fun change() {
        (getDropdown() as RowSettingGroup).refresh()
    }

    class GroupElement<T>(content: SettingsWindowContent, private val group: Group<T>, scale: Float): DropdownSettingContainer<Group<T>, Map<String, Setting<*>>>(group, scale) {
        init {
            pushChild(RowButton(this) {
                group.enabled.value = !group.enabled.value
            })
            pushChild(SubDeleteButton(scale) {
                group.groupedParent.remove(group)
                (getParent() as? RowSettingGroup)?.refresh()
            })
            setDropdown(RowSettingGroup(group, 1, content, scale * 0.87f))
        }

        override fun getText(): String = group.title.value
        override fun getTextColor(theme: Theme): Color = if(group.enabled.value) theme.primary.value else theme.lightground.value
    }

    open class ActionButton(private val text: String, action: (Button) -> Unit, scale: Float): DynamicElement(height = { scale }) {
        private val fontSize = scale * 13f/18f

        init {
            pushChild(RowButton(this, action))
        }

        override fun getWidth() = getParent()?.getWidth() ?: 0f

        override fun draw(theme: Theme, buffers: Renderer.Buffers, matrixStack: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
            // outline
            val width = getWidth()
            val height = getHeight()
            val lineColor = theme.primary.value
            buffers.lines.draw(matrixStack) {
                vertices(
                    0f, height, 0f, 2f, lineColor.red, lineColor.green, lineColor.blue, lineColor.alpha,
                    width, height, 0f, 2f, lineColor.red, lineColor.green, lineColor.blue, lineColor.alpha,
                    0f, 0f, 0f, 2f, lineColor.red, lineColor.green, lineColor.blue, lineColor.alpha,
                    width, 0f, 0f, 2f, lineColor.red, lineColor.green, lineColor.blue, lineColor.alpha
                )
                indices(
                    0, 1,
                    0, 2,
                    1, 3
                )
            }

            val color = theme.lightground.value
            val fontRenderer = theme.font.value.getRenderer()
            val x = getWidth() * 0.5 - fontRenderer.getStringWidth(text, fontSize) * 0.5
            fontRenderer.drawString(matrixStack, text, fontSize, x.toFloat(), 1f, color.red, color.green, color.blue, color.alpha)
        }
    }

    class AddGroupElement<T, V: Group<T>>(setting: GroupedSetting<T, V>, scale: Float): ActionButton("Add Group", {
        setting.add(setting.groupSupplier.get())
        ((it.getParent() as? ActionButton)?.getParent() as? RowSettingGroup)?.refresh() // so scuffed lol
    }, scale)

    class EditMembersElement<T, V: Group<T>>(content: WindowContent, group: V, scale: Float): ActionButton("Edit Members", {
        content.getWindow()?.open {
            addString("setting", group.path)
            EditMembersContent::class.java
        }
    }, scale)

    class EditMembersContent<T>(settings: MapSetting): WindowContent(settings) {
        private val name = settings.addString("setting", "") // name of Group<T>
        private val setting = Ares.SETTINGS.find(name.value) as? Group<T>
        private val group = setting?.groupedParent?.possibleMembers?.let { PossibleMembersRowGroup(setting, it, 18f, this::getWidth) }

        init {
            group?.let { pushChild(it) }
        }

        override fun getTitle() = setting?.title?.value ?: "<null>"

        override fun getHeight() = group?.getHeight() ?: 0f
    }

    class GroupMemberRowElement<T>(private val parent: GroupMembersDropdown<T>?, private val group: Group<T>, private val member: GroupMember<T>, scale: Float): RowElement(scale) {
        private val button = ToggleButton(this, scale)

        init {
            pushChild(RowButton(this) { button.click() })
            pushChild(button)
        }

        fun refresh() {
            button.refresh()
        }

        override fun getText(): String = member.name

        private class ToggleButton<T>(private val element: GroupMemberRowElement<T>, height: Float): SubToggleButton(height) {
            private var state = contains()

            override fun getState(): Boolean = state

            fun contains() = element.group.members.contains(element.member.value)

            fun refresh() {
                state = contains()
            }

            override fun setState(value: Boolean) {
                if(value) element.group.members.add(element.member.value)
                else element.group.members.remove(element.member.value)
                state = value
                element.parent?.refresh()
            }
        }
    }

    class GroupMembersDropdown<T>(private val parent: GroupMembersDropdown<T>?, private val group: Group<T>, private val members: GroupMembers<T>, scale: Float): DropdownContainer(scale) {
        private val button = ToggleButton(this, scale)

        init {
            pushChild(RowButton(this) { button.click() })
            pushChild(button)
            setDropdown(PossibleMembersRowGroup(group, members.children.sortedBy { it.name }, scale, width = this::getWidth, dropdown = this))
        }

        fun refresh() {
            button.refresh()
        }

        override fun getText(): String = members.name

        private class ToggleButton<T>(private val element: GroupMembersDropdown<T>, height: Float): SubToggleButton(height) {
            private val values = element.members.children.map { it.value }.toSet()
            private var state = contains()

            override fun getState(): Boolean = state

            fun contains() = element.group.members.containsAll(values)

            fun refresh() {
                state = contains()
            }

            override fun setState(value: Boolean) {
                if(value) element.group.members.addAll(values)
                else element.group.members.removeAll(values)
                state = value

                element.parent?.refresh()
                for(child in element.getDropdown()?.getChildren()!!) {
                    when(child) {
                        is GroupMemberRowElement<*> -> child.refresh()
                        is GroupMembersDropdown<*> -> child.refresh()
                    }
                }
            }
        }
    }

    class PossibleMembersRowGroup<T>(private val group: Group<T>, private val members: Iterable<IGroupMember<T>>, private val scale: Float, width: () -> Float,
                                     private val dropdown: GroupMembersDropdown<T>? = null): DynamicElementGroup(1, width = width) {
        init {
            refresh()
        }

        private fun refresh() {
            for(member in members) {
                if(member is GroupMember<T>) pushChild(GroupMemberRowElement(dropdown, group, member, scale * 0.87f))
                else pushChild(GroupMembersDropdown(dropdown, group, member as GroupMembers<T>, scale * 0.87f))
            }
        }
    }
}
