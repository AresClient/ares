package dev.tigr.ares.core.feature;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import dev.tigr.ares.Wrapper;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.SettingCategory;
import dev.tigr.ares.core.setting.settings.StringSetting;
import dev.tigr.ares.core.util.global.Manager;
import dev.tigr.ares.core.util.render.TextColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author Tigermouthbear
 */
public class Command implements Wrapper {
    public static final SettingCategory SETTING_CATEGORY = new SettingCategory("Command");
    public static final Setting<String> PREFIX = new StringSetting(SETTING_CATEGORY, "prefix", "-");
    public static final CommandDispatcher DISPATCHER = new CommandDispatcher<>();
    public static final List<CommandNode> COMMAND_NODES = new ArrayList<>();
    public static final Manager<Command> MANAGER = new Manager<>();

    private final String name;
    private final String description;

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static void execute(String command) {
        String messageNoPrefix = command.startsWith(PREFIX.getValue()) ? command.substring(PREFIX.getValue().length()) : command;

        try {
            // give baritone the args if its a baritone command
            if(messageNoPrefix.startsWith("b ")) UTILS.executeBaritoneCommand(messageNoPrefix.replaceFirst("b ", ""));
            else DISPATCHER.execute(messageNoPrefix, null);
        } catch(CommandSyntaxException e) {
            CommandNode commandNode = getNodeByName(messageNoPrefix.split(" ")[0]);

            if(commandNode == null)
                UTILS.printMessage(TextColor.RED + "Invalid command! Type " + PREFIX.getValue() + "help for a list of commands");
            else
                UTILS.printMessage(TextColor.BLUE + "Usage: " + TextColor.RED + getUsage(commandNode));
        }
    }

    public static CommandNode getParentNode(CommandNode commandNode) {
        while(commandNode.getRedirect() != null) commandNode = commandNode.getRedirect();

        return commandNode;
    }

    public static CommandNode getNodeByName(String name) {
        Optional<CommandNode> optional = COMMAND_NODES.stream().filter(cn -> cn.getName().equalsIgnoreCase(name)).findAny();
        return optional.orElse(null);
    }

    public static String getUsage(CommandNode commandNode) {
        commandNode = getParentNode(commandNode);

        StringBuilder sb = new StringBuilder(commandNode.getName());
        sb.append(" ");

        List<List<String>> map = new ArrayList<>();

        String[] usages = DISPATCHER.getAllUsage(commandNode, null, false);
        for(int x = 0; x < usages.length; x++) {
            String[] split = usages[x].split(" ");
            for(int y = 0; y < split.length; y++) {
                String arg = split[y].replaceAll("<", "").replaceAll(">", "");
                if(x == 0) map.add(y, new ArrayList<>());
                map.get(y).add(x, arg);
            }
        }

        for(List<String> unfilteredArgs: map) {
            List<String> args = new ArrayList<>();
            for(String arg: unfilteredArgs) {
                if(!args.contains(arg)) args.add(arg);
            }

            if(args.size() > 1) {
                sb.append("[");
                for(int i = 0; i < args.size(); i++) {
                    sb.append(args.get(i));
                    if(i + 1 != args.size()) sb.append("/");
                }
                sb.append("]");
            } else sb.append("<").append(args.get(0)).append(">");

            if(map.indexOf(unfilteredArgs) != map.size() - 1) sb.append(" ");
        }

        return sb.toString();
    }

    public static String complete(String text) {
        if(text.startsWith(PREFIX.getValue())) text = text.replaceFirst(PREFIX.getValue(), "");
        if(text.isEmpty()) return "";

        String[] split = text.contains(" ") ? text.split(" ") : new String[]{text};
        int length = text.endsWith(" ") ? split.length + 1 : split.length;

        CommandNode commandNode = getNodeByName(split[0]);
        if(commandNode == null) return completeName(text);

        String[] args = getUsage(commandNode).split(" ");

        StringBuilder sb = new StringBuilder(text.endsWith(" ") ? "" : " ");
        for(int i = 1; i < args.length; i++) {
            if(i > length - 2 && !(i == length - 1 && !text.endsWith(" "))) sb.append(args[i]).append(" ");
        }

        return sb.toString();
    }

    public static String completeName(String text) {
        List<String> names = new ArrayList<>();
        COMMAND_NODES.forEach(cn -> names.add(cn.getName()));

        names.sort(new StringLengthComparator());

        for(String name: names) {
            if(name.startsWith(text)) return name.replaceFirst(text, "");
        }
        return "";
    }

    protected CommandNode register(LiteralArgumentBuilder literalArgumentBuilder) {
        CommandNode commandNode = DISPATCHER.register(literalArgumentBuilder);
        COMMAND_NODES.add(commandNode);
        return commandNode;
    }

    private static class StringLengthComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            return Integer.compare(o1.length(), o2.length());
        }
    }
}
