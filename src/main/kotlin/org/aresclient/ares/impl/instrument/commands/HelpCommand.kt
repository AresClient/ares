package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.Ares
import org.aresclient.ares.api.instruments.Command

object HelpCommand: Command("help", "h", "?") {
    override fun LiteralArgumentBuilder<IContext>.builder(): LiteralArgumentBuilder<IContext?> {
        return then(argument<IContext, String>("command", string()).executes {
            val name = getString(it, "command")
            val command = Ares.PLUGINS
                .flatMap { plugin -> plugin.commands }
                .find { command -> command.name == name || command.aliases.contains(name) }
            if(command == null) {
                it.source.error("Unknown command $name")
                return@executes 1
            }

            getUsages(it.source, command.getNode()).forEach { usage ->
                it.source.print("${command.name} $usage")
            }
            1
        }).executes {
            Ares.PLUGINS.forEach { plugin ->
                it.source.print("${plugin.name} Commands:")
                plugin.commands.forEach { command ->
                    val usages = command.getUsages(it.source)
                    if(usages.isEmpty()) it.source.print(command.name)
                    else usages.forEach { usage ->
                        it.source.print("${command.getNode().name} $usage")
                    }
                }
            }
            1
        }
    }
}
