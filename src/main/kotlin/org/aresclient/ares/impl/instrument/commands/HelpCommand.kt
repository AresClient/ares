package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.Ares
import org.aresclient.ares.api.instruments.Command

object HelpCommand: Command(register(
    literal<IContext?>("help")
        .then(argument<IContext?, String?>("command", string())
        .executes { with(it.source) {
            val command = getCommand(getString(it, "command"))
            getUsages(this, command).forEach { usage ->
                print("${command.name} $usage")
            }
            1
        }}).executes { with(it.source) {
            Ares.PLUGINS.forEach { plugin ->
                print("${plugin.name} Commands:")
                plugin.commands.forEach { command ->
                    command.getUsages(this).forEach { usage ->
                        print("${command.getNode().name} $usage")
                    }
                }
            }
            1
        }}
))