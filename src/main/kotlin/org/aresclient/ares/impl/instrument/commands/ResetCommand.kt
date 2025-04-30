package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.Ares
import org.aresclient.ares.api.instruments.Command

object ResetCommand: Command("reset") {
    override fun LiteralArgumentBuilder<IContext>.builder(): LiteralArgumentBuilder<IContext?> {
        return then(argument<IContext, String?>("setting", string()).executes {
            val path = getString(it, "setting")
            Ares.SETTINGS.find(path)?.let { setting ->
                setting.setDefault()
                it.source.print("Successfully reset setting $path")
            } ?: it.source.error("Failed to find setting with name $path")
            1
        }).executes {
            Ares.SETTINGS.setDefault()
            it.source.print("Successfully reset all settings")
            1
        }
    }
}
