package org.aresclient.ares.impl.command

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.api.command.Command

object EchoCommand: Command(register(
    literal<IContext?>("echo")
        .then(argument<IContext?, String>("text", string())
        .executes { with(it.source) {
            print(getString(it, "text"))
            1
        }})
))