package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.Ares
import org.aresclient.ares.api.instruments.Command
import java.io.File

object SaveCommand: Command("save") {
    override fun LiteralArgumentBuilder<IContext>.builder(): LiteralArgumentBuilder<IContext?> {
        return then(argument<IContext, String?>("config", string()).executes {
            save(it.source, getString(it, "config"))
            1
        }).executes {
            save(it.source, "settings")
            1
        }
    }

    private fun save(context: IContext, name: String) {
        val file = File("ares/config/$name.json")
        file.parentFile.mkdirs()

        try {
            Ares.getSettings().write(file)
        } catch(e: Exception) {
            context.error("Failed to save config file")
            return
        }

        context.print("Successfully saved config file")
    }
}
