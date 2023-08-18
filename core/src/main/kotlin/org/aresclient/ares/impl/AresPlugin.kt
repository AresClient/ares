package org.aresclient.ares.impl

import net.meshmc.mesh.loader.Mod
import net.meshmc.mesh.loader.Mod.Instance
import org.aresclient.ares.api.Ares
import org.aresclient.ares.impl.command.EchoCommand
import org.aresclient.ares.impl.command.HelpCommand
import org.aresclient.ares.impl.module.misc.ClickGUI
import org.aresclient.ares.impl.module.misc.TitleScreen
import org.aresclient.ares.impl.module.player.AntiAFK
import org.aresclient.ares.impl.module.render.ESP
import org.aresclient.ares.impl.module.render.TestModule

class AresPlugin: Ares.Plugin(), Mod.Initializer {
    companion object {
        @Instance
        lateinit var INSTANCE: AresPlugin
    }

    override fun init() {
        modules.addAll(arrayOf(
            AntiAFK,
            ClickGUI,
            ESP,
            TestModule,
            TitleScreen
        ))

        commands.addAll(arrayOf(
            HelpCommand,
            EchoCommand
        ))
    }
}
