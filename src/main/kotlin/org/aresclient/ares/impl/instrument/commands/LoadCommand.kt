package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.Ares
import org.aresclient.ares.api.instruments.Command
import java.io.File

object LoadCommand: Command(register(
    literal<IContext?>("load")
        .then(argument<IContext?, String?>("config", string())
        .executes {
            LoadCommand.load(it.source, getString(it, "config"))
            1
        })
)) {
    private fun load(context: IContext, name: String) {
        val file = File("ares/config/$name.json")
        if(!file.exists()) {
            context.error("Config $name does not exist")
            return
        }

        try {
            Ares.SETTINGS.read(file)
        } catch(e: Exception) {
            context.error("Failed to read config file")
            return
        }

        context.print("Successfully loaded config file")
    }
}
