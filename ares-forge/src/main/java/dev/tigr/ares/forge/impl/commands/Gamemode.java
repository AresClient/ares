package dev.tigr.ares.forge.impl.commands;

import dev.tigr.ares.core.feature.Command;
import net.minecraft.world.GameType;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

/**
 * @author Tigermouthbear
 */
public class Gamemode extends Command {
    public Gamemode() {
        super("gamemode", "Changes the client side gamemode");

        register(literal("gm").redirect(register(literal("gamemode").then(
                literal("c").executes(c -> {
                    MC.playerController.setGameType(GameType.CREATIVE);
                    return 1;
                })).then(literal("s").executes(c -> {
                    MC.playerController.setGameType(GameType.SURVIVAL);
                    return 1;
                }))
        )));
    }
}
