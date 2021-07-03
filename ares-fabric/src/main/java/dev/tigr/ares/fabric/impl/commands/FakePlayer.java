package dev.tigr.ares.fabric.impl.commands;

import com.mojang.authlib.GameProfile;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.util.global.MojangApi;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

/**
 * @author Tigermouthbear
 */
public class FakePlayer extends Command {
    private final List<Integer> ids = new ArrayList<>();

    public FakePlayer() {
        super("fakeplayer", "Creates an NPC of the specified user");

        register(literal("fp").redirect(register(literal("fakeplayer").then(
                argument("name", string()).executes(c -> {
                    String player = getString(c, "name");
                    UUID playerUUID = MojangApi.stringToUUID(MojangApi.getUuid(player));
                    OtherClientPlayerEntity fakePlayer = new OtherClientPlayerEntity(MC.world, new GameProfile(playerUUID, player));
                    fakePlayer.copyFrom(MC.player);

                    NbtCompound compoundTag = new NbtCompound();
                    MC.player.writeCustomDataToNbt(compoundTag);
                    fakePlayer.readCustomDataFromNbt(compoundTag);

                    MC.world.addEntity(getRandomId(), fakePlayer);
                    return 1;
                }))
        )));
    }

    private int getRandomId() {
        int num = (int) (Math.random() * ((1000 - 10) + 1)) + 10;
        if(ids.contains(num)) num = getRandomId();
        ids.add(num);
        return -num;
    }
}
