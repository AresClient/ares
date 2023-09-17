package org.aresclient.ares.impl

import net.meshmc.mesh.loader.Mod
import net.meshmc.mesh.loader.Mod.Instance
import org.aresclient.ares.api.Ares
import org.aresclient.ares.impl.command.EchoCommand
import org.aresclient.ares.impl.command.HelpCommand
import org.aresclient.ares.impl.instrument.global.Interaction
import org.aresclient.ares.impl.instrument.global.Priority
import org.aresclient.ares.impl.instrument.global.Render
import org.aresclient.ares.impl.instrument.global.Rotation
import org.aresclient.ares.impl.instrument.module.modules.misc.ClickGUI
import org.aresclient.ares.impl.instrument.module.modules.misc.TitleScreen
import org.aresclient.ares.impl.instrument.module.modules.offense.CrystalAura
import org.aresclient.ares.impl.instrument.module.modules.player.AntiAFK
import org.aresclient.ares.impl.instrument.module.modules.render.ESP
import org.aresclient.ares.impl.instrument.module.modules.render.TestModule

class AresPlugin: Ares.Plugin(), Mod.Initializer {
    companion object {
        @Instance
        lateinit var INSTANCE: AresPlugin
    }

    // TODO: fix seam in skybox corners on title screen
    override fun init() {
        globals.addAll(arrayOf(
            Interaction,
            Priority,
            Render,
            Rotation
        ))

        modules.addAll(arrayOf(
            AntiAFK,
            ClickGUI,
            CrystalAura,
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
