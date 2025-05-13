package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.instruments.Command
import org.aresclient.ares.impl.gui.hud.AresHudScreen

object HudCommand: Command("hud"), Wrapper {
    override fun LiteralArgumentBuilder<IContext>.builder(): LiteralArgumentBuilder<IContext?> {
        return executes {
            Thread {
                Thread.sleep(500)
                MC.executeSync {
                    MC.setScreen(AresHudScreen())
                }
            }.start()
            1
        }
    }
}
