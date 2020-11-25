package dev.tigr.ares.forge.impl.commands;

import dev.tigr.ares.core.feature.Command;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

/**
 * @author Tigermouthbear
 */
public class BaritoneCommand extends Command {
    public BaritoneCommand() {
        super("b", "Baritone command interface");

        register(literal("b"));
    }
}
