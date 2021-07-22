package dev.tigr.ares.fabric.impl.commands;

import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.feature.FriendManager;
import dev.tigr.ares.core.util.render.TextColor;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 * @author Tigermouthbear
 */
public class Friend extends Command {
    public Friend() {
        super("friend", "Add, del, and list from the players on your friends list");

        register(literal("f").redirect(register(literal("friend").then(
                literal("add").then(argument("player", string()).executes(c -> {
                    String player = getString(c, "player");

                    if(!FriendManager.isFriend(player)) {
                        FriendManager.addFriend(player);
                        UTILS.printMessage(TextColor.GREEN + "Added " + TextColor.BLUE + player + TextColor.GREEN + " to your friends!");
                    } else
                        UTILS.printMessage(TextColor.BLUE + player + TextColor.GREEN + " was already a friend");

                    return 1;
                }))).then(
                literal("del").then(argument("player", string()).executes(c -> {
                    String player = getString(c, "player");

                    if(FriendManager.isFriend(player)) {
                        FriendManager.removeFriend(player);
                        UTILS.printMessage(TextColor.RED + "Removed " + TextColor.BLUE + player + TextColor.RED + " from your friends!");
                    } else
                        UTILS.printMessage(TextColor.BLUE + player + TextColor.RED + " wasn't a friend");

                    return 1;
                }))).then(
                literal("list").executes(c -> {
                    StringBuilder message = new StringBuilder("Friends: ");
                    for(int x = 0; x <= FriendManager.getFriends().size() - 1; x++) {
                        if(x == FriendManager.getFriends().size() - 1) {
                            message.append(FriendManager.getFriends().get(x));
                            break;
                        }
                        message.append(FriendManager.getFriends().get(x)).append(", ");
                    }

                    UTILS.printMessage(TextColor.GREEN + message.toString());

                    return 1;
                })))
        ));
    }
}
