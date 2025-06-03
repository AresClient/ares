package org.aresclient.ares.impl.instrument.modules.misc

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import org.aresclient.ares.api.events.ToggleEvent
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.nrender.font.TextColor
import org.aresclient.ares.impl.util.ChatUtil

object ToggleNotifications: Module(Category.MISC, "Toggle Notifications", "Sends a chat message when a module is toggled", Defaults().setEnabled(true)) {
    @field:EventHandler
    private val toggleListener = EventListener<ToggleEvent> { event ->
        if(event.module == ClickGUI) return@EventListener
        ChatUtil.print("${if(event.enabled) "${TextColor.GREEN}Enabled" else "${TextColor.RED}Disabled"} ${TextColor.BLUE}${event.module.name}")
    }

    //TODO: HUD Element which works independently from the chat
}
