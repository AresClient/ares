package dev.tigr.ares.forge.impl.commands;

import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.impl.modules.render.Search;
import net.minecraft.block.Block;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 * @author Tigermouthbear
 */
public class SearchCommand extends Command {
    public SearchCommand() {
        super("search", "Add or remove blocks from the search list");

        register(literal("search").then(
                literal("add").then(argument("block", string()).executes(c -> {
                    Block block = Block.getBlockFromName(getString(c, "block"));

                    if(block == null) UTILS.printMessage(TextColor.RED + "Block not Found!");
                    else {
                        if(Search.add(block))
                            UTILS.printMessage(TextColor.GREEN + "Added " + TextColor.BLUE + block.getLocalizedName() + TextColor.GREEN + " to search list!");
                        else UTILS.printMessage(TextColor.GREEN + "Block is already in search list!");
                    }

                    return 1;
                }))).then(
                literal("del").then(argument("block", string()).executes(c -> {
                    Block block = Block.getBlockFromName(getString(c, "block"));

                    if(block == null) UTILS.printMessage(TextColor.RED + "Block not Found!");
                    else {
                        if(Search.del(block))
                            UTILS.printMessage(TextColor.RED + "Deleted " + TextColor.BLUE + block.getLocalizedName() + TextColor.RED + " from search list!");
                        else UTILS.printMessage(TextColor.RED + "Block isn't in search list!");
                    }

                    return 1;
                }))).then(
                literal("list").executes(c -> {
                    UTILS.printMessage(list());
                    return 1;
                })
        ));
    }

    public static String list() {
        if(Search.getBlocks().isEmpty()) return "No blocks in search list!";

        StringBuilder sb = new StringBuilder("Blocks: ").append(TextColor.BLUE);
        for(Block block: Search.getBlocks()) {
            if(block != null) sb.append(block.getLocalizedName()).append(", ");
        }

        return sb.toString();
    }
}
