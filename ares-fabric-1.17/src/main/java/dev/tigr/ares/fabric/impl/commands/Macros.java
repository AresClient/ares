package dev.tigr.ares.fabric.impl.commands;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.client.PostInitializationEvent;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.BindSetting;
import dev.tigr.ares.core.setting.settings.ListSetting;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 * @author Tigermouthbear
 */
public class Macros extends Command {
    private static final Setting<List<String>> setting = new ListSetting<>(Command.SETTING_CATEGORY, "macros", new ArrayList<>());
    private static final String regex = "==";

    private final List<Setting<String>> macros = new ArrayList<>();
    @EventHandler
    public EventListener<PostInitializationEvent> postInitializationEvent = new EventListener<>(event -> {
        // load macros from settings after settings are loaded
        for(String unparsed: setting.getValue()) {
            int index = unparsed.indexOf(regex);
            String macro = unparsed.substring(index + regex.length());
            String key = unparsed.substring(0, index);

            macros.add(new BindSetting(macro, key, setting -> {
                if(macro.startsWith(Command.PREFIX.getValue())) Command.execute(macro);
                MC.player.sendChatMessage(macro);
            }));
        }
    });

    public Macros() {
        super("macro", "Sends a message or command to chat on key press");

        Ares.EVENT_MANAGER.register(this);

        register(literal("macro").then(
                literal("add").then(argument("key", string()).then(argument("macro", greedyString()).executes(c -> {
                    UTILS.printMessage(add(getString(c, "key"), getString(c, "macro")));
                    return 1;
                })))).then(
                literal("del").then(argument("key", string()).executes(c -> {
                    UTILS.printMessage(del(getString(c, "key")));
                    return 1;
                }))).then(
                literal("list").executes(c -> {
                    UTILS.printMessage(list());
                    return 1;
                })
        ));
    }

    public String add(String key, String macro) {
        if(setting.getValue().contains(encode(key, macro)))
            return TextColor.GREEN + "Macro " + TextColor.BLUE + key + ": " + macro + TextColor.GREEN + " already added!";

        setting.getValue().add(encode(key, macro));
        macros.add(new BindSetting(macro, key, setting -> {
            if(macro.startsWith(Command.PREFIX.getValue())) Command.execute(macro);
            MC.player.sendChatMessage(macro);
        }));

        return TextColor.GREEN + "Added macro " + TextColor.BLUE + key + ": " + macro;
    }

    public String del(String key) {
        Optional<String> optional = setting.getValue().stream().filter(macro -> macro.substring(0, macro.indexOf(regex)).equalsIgnoreCase(key)).findAny();

        if(!optional.isPresent())
            return TextColor.RED + "Macro binded to key " + TextColor.BLUE + key + TextColor.RED + " not found!";

        setting.getValue().remove(optional.get());
        for(Setting<String> bind: new ArrayList<>(macros)) {
            if(optional.get().equalsIgnoreCase(encode(bind.getValue(), bind.getName()))) {
                macros.remove(bind);
                bind.remove();
            }
        }

        return TextColor.RED + "Removed macro binded to key " + TextColor.BLUE + key;
    }

    public String list() {
        if(macros.isEmpty()) return "No macros found!";

        StringBuilder sb = new StringBuilder("Macros:");
        for(Setting<String> macro: macros)
            sb.append("\n").append(TextColor.BLUE).append(macro.getValue()).append(TextColor.WHITE + ": ").append(macro.getName());

        return sb.toString();
    }

    public String encode(String key, String macro) {
        return key + regex + macro;
    }
}
