package dev.tigr.ares.fabric.impl.commands;

import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.TextColor;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 * @author Tigermouthbear
 */
public class Bind extends Command {
    public Bind() {
        super("bind", "Bind modules to a new key");

        register(literal("bind").then(
                argument("module", string()).then(argument("key", string()).executes(c -> {
                    String input = getString(c, "module");
                    String key = getString(c, "key").toUpperCase();

                    for(Module module: Module.MANAGER.getInstances()) {
                        if(input.equalsIgnoreCase(module.getName())) {
                            module.getBind().setValue(key);
                            UTILS.printMessage(TextColor.GREEN + "Bound " + TextColor.BLUE + module.getName() + TextColor.GREEN + " to " + key);
                            return 1;
                        }
                    }

                    UTILS.printMessage(TextColor.RED + "Module not found!");
                    return 1;
                }))
        ));
    }
}
