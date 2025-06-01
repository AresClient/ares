package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import org.aresclient.ares.api.instruments.Command
import org.aresclient.ares.api.nrender.TextColor
import org.aresclient.ares.impl.util.FriendUtil

object FriendCommand: Command("friend", "f") {
    override fun LiteralArgumentBuilder<IContext>.builder(): LiteralArgumentBuilder<IContext?> {
        return then(literal<IContext>("add").then(argument<IContext, String?>("player", string()).executes {
            val player: String = getString(it, "player")
            if(!FriendUtil.isFriendByName(player)) {
                if(FriendUtil.addFriendByName(player)) it.source.print("${TextColor.GREEN}Added ${TextColor.BLUE}$player ${TextColor.GREEN}to your friends")
                else it.source.error("Failed to add $player to friends")
            } else it.source.print("${TextColor.BLUE}$player ${TextColor.GREEN}is already a friend")
            1
        })).then(literal<IContext>("del").then(argument<IContext, String?>("player", string()).executes {
            val player: String = getString(it, "player")
            if(FriendUtil.isFriendByName(player)) {
                FriendUtil.removeFriendByName(player)
                it.source.print("${TextColor.RED}Removed ${TextColor.BLUE}$player ${TextColor.RED}from your friends")
            } else it.source.print("${TextColor.BLUE}$player ${TextColor.RED}isn't a friend")
            1
        })).then(literal<IContext>("list").executes {
            it.source.print("Friends:")
            FriendUtil.getFriends().forEach { friend -> it.source.print("${TextColor.BLUE}${friend.name}") }
            1
        })
    }
}
