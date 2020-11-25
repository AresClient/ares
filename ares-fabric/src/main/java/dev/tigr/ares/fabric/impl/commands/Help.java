package dev.tigr.ares.fabric.impl.commands;

import com.mojang.brigadier.tree.CommandNode;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.util.render.TextColor;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 * @author Tigermouthbear
 */
public class Help extends Command {
    public Help() {
        super("help", "Returns a list of commands");

        register(literal("help").then(
                argument("command", string()).executes(c -> {
                    String command = getString(c, "command");

                    CommandNode commandNode = getNodeByName(command);
                    if(commandNode == null) UTILS.printMessage(TextColor.RED + "Command not found!");
                    else
                        UTILS.printMessage(TextColor.BLUE + "Usage: " + TextColor.WHITE + getUsage(commandNode));

                    return 1;
                })
                ).executes(c -> {
                    StringBuilder text = new StringBuilder("Commands:\n");
                    Command.MANAGER.getInstances().forEach(command -> text.append(TextColor.BLUE).append(command.getName()).append(TextColor.WHITE).append(": ").append(command.getDescription()).append("\n"));
                    text.deleteCharAt(text.lastIndexOf("\n"));
                    UTILS.printMessage(text.toString());

                    return 1;
                })
        );
    }
}
