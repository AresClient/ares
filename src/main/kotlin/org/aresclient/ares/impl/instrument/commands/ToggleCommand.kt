package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.api.instruments.Command
import org.aresclient.ares.api.instruments.Module

object ToggleCommand: Command(register(
    literal<IContext?>("t").redirect(register(literal<IContext?>("toggle")
        .then(argument<IContext?, String?>("module", string())
        .executes {
            val name = getString(it, "module")
            Module.Category.getAll().forEach { category ->
                category.modules.forEach { module ->
                    if(module.name.equals(name, ignoreCase = true)) {
                        module.toggle()
                        it.source.print("Toggled ${module.name}")
                        return@executes 1
                    }
                }
            }
            it.source.error("No module named $name")
            1
        })
))))
