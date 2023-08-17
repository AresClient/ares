package org.aresclient.ares.impl.command

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.api.Ares
import org.aresclient.ares.api.command.Command

object HelpCommand: Command(register(
    literal<IContext?>("help")
        .then(argument<IContext?, String?>("command", string())
        .executes { with(it.source) {
            getUsages(this, getCommand(getString(it, "command"))).forEach { usage ->
                print(usage)
            }
            1
        }}).executes { with(it.source) {
            Ares.getPlugins().forEach { plugin ->
                print("${plugin.name} Commands:")
                plugin.commands.forEach { command ->
                    command.getUsages(this).forEach { usage ->
                        print(usage)
                    }
                }
            }
            1
        }}
))