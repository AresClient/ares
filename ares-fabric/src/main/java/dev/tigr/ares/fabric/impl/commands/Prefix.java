package dev.tigr.ares.fabric.impl.commands;

import dev.tigr.ares.core.feature.Command;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 * @author Tigermouthbear 7/22/20
 */
public class Prefix extends Command {
    public Prefix() {
        super("prefix", "Change the command prefix");

        register(literal("prefix").then(argument("prefix", string()).executes(c -> {
            String prefix = getString(c, "prefix");

            if(prefix.length() == 1) {
                Command.PREFIX.setValue(prefix);
            } else UTILS.printMessage("Prefix must be one character long");
            return 1;
        })));
    }
}
