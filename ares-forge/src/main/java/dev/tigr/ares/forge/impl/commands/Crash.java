package dev.tigr.ares.forge.impl.commands;

import dev.tigr.ares.core.feature.Command;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

/**
 * The most complex logic.
 * Quakes inverse square root is nothing compared to this masterpiece
 *
 * @author UberRipper
 */
public class Crash extends Command {

    public Crash() {
        super("crash", "crashes your game");

        register(literal("crash").executes(c -> {
                    System.exit(0);
                    return 0;
                })
        );
    }
}
