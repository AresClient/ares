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
public class Save extends Command {
    public Save() {
        super("save", "Saves a config to a file");

        register(literal("save").then(argument("name", string()).executes(c -> {
            try {
                SettingsManager.save(new File("Ares/config-" + getString(c, "name") + ".json"));
                UTILS.printMessage(TextColor.GREEN + "Config saved successfully");
            } catch(IOException e) {
                UTILS.printMessage(TextColor.RED + "Error saving config");
            }
            return 1;
        })));
    }
}
