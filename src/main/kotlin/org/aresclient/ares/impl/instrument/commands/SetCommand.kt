package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.Ares
import org.aresclient.ares.api.instruments.Command

object SetCommand: Command("set") {
    override fun LiteralArgumentBuilder<IContext>.builder(): LiteralArgumentBuilder<IContext?> {
        return then(argument<IContext, String?>("setting", string()).then(argument<IContext, String?>("value", string()).executes {
            val path = getString(it, "setting")
            val value = getString(it, "value")
            Ares.getSettings().find(path)?.let { setting ->
                try {
                    setting.read(value)
                    it.source.print("Successfully set ${setting.path} to $value")
                } catch(_: Exception) {
                    it.source.error("Failed to set ${setting.path} to $value")
                }
            } ?: it.source.error("Failed to find setting with name $path")
            1
        }))
    }
}
