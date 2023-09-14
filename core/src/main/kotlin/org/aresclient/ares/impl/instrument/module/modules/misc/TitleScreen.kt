package org.aresclient.ares.impl.instrument.module.modules.misc

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import org.aresclient.ares.api.Ares
import org.aresclient.ares.api.event.client.ScreenOpenedEvent
import org.aresclient.ares.api.instrument.module.Category
import org.aresclient.ares.api.instrument.module.Module
import org.aresclient.ares.impl.gui.impl.title.AresTitleScreen

object TitleScreen: Module(Category.MISC, "TitleScreen", "Replace the default Minecraft title screen with an Ares themed title screen",
    Defaults().setEnabled(true)) {
    private val screen by lazy { AresTitleScreen() }

    @field:EventHandler
    private val screenOpenedEventListener = EventListener<ScreenOpenedEvent> { event ->
        if(event.isMainMenu) Ares.getMinecraft().openScreen(screen.getScreen())
    }
}
