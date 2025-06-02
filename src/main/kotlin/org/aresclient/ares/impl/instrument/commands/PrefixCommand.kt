package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.Ares
import org.aresclient.ares.api.instruments.Command

object PrefixCommand: Command("prefix") {
    override fun LiteralArgumentBuilder<IContext>.builder(): LiteralArgumentBuilder<IContext?> {
        return then(argument<IContext, String?>("prefix", string()).executes {
            val prefix = getString(it, "prefix")
            Ares.getCommandPrefixSetting().value = prefix
            it.source.print("Set command prefix to $prefix")
            1
        })
    }
}
