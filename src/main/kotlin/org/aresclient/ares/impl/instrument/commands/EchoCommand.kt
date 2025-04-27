package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.api.instruments.Command

object EchoCommand: Command(register(
    literal<IContext?>("echo")
        .then(argument<IContext?, String>("text", string())
        .executes { with(it.source) {
            print(getString(it, "text"))
            1
        }})
))