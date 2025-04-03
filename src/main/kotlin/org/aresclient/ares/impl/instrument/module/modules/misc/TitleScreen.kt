package org.aresclient.ares.impl.instrument.module.modules.misc

import dev.tigr.simpleevents.listener.EventHandler
import dev.tigr.simpleevents.listener.EventListener
import org.aresclient.ares.api.events.ScreenOpenedEvent
import org.aresclient.ares.api.instruments.Module
import org.aresclient.ares.api.util.ScreenAdapter
import org.aresclient.ares.impl.gui.title.AresTitleScreen

object TitleScreen: Module(Category.MISC, "TitleScreen", "Replace the default Minecraft title screen with an Ares themed title screen",
    Defaults().setEnabled(true)) {
    private val screen by lazy { AresTitleScreen() }

    @EventHandler private val screenOpenedEventListener = EventListener<ScreenOpenedEvent> { event ->
        if(event.mainMenu) MC.setScreen(ScreenAdapter(screen.getScreen()))
    }
}
