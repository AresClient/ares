package org.aresclient.ares.impl.instrument.module.modules.misc

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import org.aresclient.ares.api.events.ToggleEvent
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.render.TextColor
import org.aresclient.ares.impl.util.ChatUtil

object MsgOnToggle: Module(Category.MISC, "MsgOnToggle", "Sends a chat message when a module is toggled") {
    @field:EventHandler
    private val toggleListener = EventListener<ToggleEvent> { event ->
        if(event.module == ClickGUI) return@EventListener
        ChatUtil.print("${if(event.enabled) "${TextColor.GREEN}Enabled" else "${TextColor.RED}Disabled"} ${TextColor.BLUE}${event.module.name}")
    }
}
