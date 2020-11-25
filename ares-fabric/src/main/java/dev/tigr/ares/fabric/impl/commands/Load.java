package dev.tigr.ares.fabric.impl.commands;

import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.setting.SettingsManager;
import dev.tigr.ares.core.util.render.TextColor;

import java.io.File;
import java.io.IOException;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 * @author Tigermouthbear 7/5/20
 */
public class Load extends Command {
    public Load() {
        super("load", "Loads a config file previously saved");

        register(literal("load").then(argument("name", string()).executes(c -> {
            try {
                SettingsManager.load(new File("Ares/config-" + getString(c, "name") + ".json"));
                UTILS.printMessage(TextColor.GREEN + "Config loaded successfully");
            } catch(IOException e) {
                UTILS.printMessage(TextColor.RED + "Config not found!");
            }
            return 1;
        })));
    }
}
