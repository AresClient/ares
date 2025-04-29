package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import org.aresclient.ares.api.instruments.Command

object ClearCommand: Command("clear") {
    override fun LiteralArgumentBuilder<IContext>.builder(): LiteralArgumentBuilder<IContext?> {
        return executes {
            it.source.clear()
            1
        }
    }
}
