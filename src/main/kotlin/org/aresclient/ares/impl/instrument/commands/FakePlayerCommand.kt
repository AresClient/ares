package org.aresclient.ares.impl.instrument.commands

import com.mojang.authlib.GameProfile
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.instruments.Command
import java.util.*

object FakePlayerCommand: Command("fakeplayer", "fp"), Wrapper {
    private val fakeplayers = mutableListOf<OtherClientPlayerEntity>()

    override fun LiteralArgumentBuilder<IContext>.builder(): LiteralArgumentBuilder<IContext?> {
        return then(argument<IContext?, String?>("name", string()).executes {
            fakeplayer(getString(it, "name"))
        }).then(literal<IContext?>("clear").executes {
            fakeplayers.forEach {
                MC.world?.removeEntity(it.id, Entity.RemovalReason.KILLED)
            }
            fakeplayers.clear()
            1
        }).executes {
            fakeplayer("FakePlayer")
        }
    }

    private fun fakeplayer(name: String): Int {
        val player = OtherClientPlayerEntity(MC.world, GameProfile(UUID.randomUUID(), name))
        player.copyFrom(MC.player)

        val data = NbtCompound()
        MC.player?.writeCustomDataToNbt(data)
        player.readCustomDataFromNbt(data)

        MC.world?.addEntity(player)
        fakeplayers.add(player)
        return 1
    }
}
