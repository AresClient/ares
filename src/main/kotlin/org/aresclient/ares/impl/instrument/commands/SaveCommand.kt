package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.Ares
import org.aresclient.ares.api.instruments.Command
import java.io.File

object SaveCommand: Command(register(
    literal<IContext?>("save")
        .then(argument<IContext?, String?>("config", string())
        .executes {
            SaveCommand.save(it.source, getString(it, "config"))
            1
        }).executes {
            SaveCommand.save(it.source, "settings")
            1
        }
)) {
    private fun save(context: IContext, name: String) {
        val file = File("ares/config/$name.json")
        file.parentFile.mkdirs()

        try {
            Ares.SETTINGS.write(file)
        } catch(e: Exception) {
            context.error("Failed to save config file!")
            return
        }

        context.print("Successfully saved config file!")
    }
}
