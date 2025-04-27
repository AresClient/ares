package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.Ares
import org.aresclient.ares.api.instruments.Command

object PrefixCommand: Command(register(
    literal<IContext?>("prefix")
        .then(argument<IContext?, String?>("prefix", string())
        .executes {
            val prefix = getString(it, "prefix")
            Ares.COMMAND_PREFIX.value = prefix
            it.source.print("Set command prefix to $prefix")
            1
        })
))
