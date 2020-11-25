package dev.tigr.ares.forge.impl.commands;

import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.impl.modules.misc.MsgOnToggle;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 * @author Tigermouthbear
 */
public class Toggle extends Command {
    public Toggle() {
        super("toggle", "Toggles the desired module");

        register(literal("t").redirect(register(literal("toggle").then(
                argument("module", string()).executes(c -> {
                    String input = getString(c, "module");

                    for(Module module: Module.MANAGER.getInstances()) {
                        if(input.equalsIgnoreCase(module.getName())) {
                            module.toggle();

                            if(!MsgOnToggle.INSTANCE.getEnabled()) {
                                if(module.getEnabled())
                                    UTILS.printMessage(TextColor.GREEN + "Enabled " + TextColor.BLUE + module.getName());
                                else
                                    UTILS.printMessage(TextColor.RED + "Disabled " + TextColor.BLUE + module.getName());
                            }

                            return 1;
                        }
                    }

                    UTILS.printMessage(TextColor.RED + "Module not found!");
                    return 1;
                })))
        ));
    }
}
