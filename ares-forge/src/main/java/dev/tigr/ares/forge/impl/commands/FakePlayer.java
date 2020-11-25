package dev.tigr.ares.forge.impl.commands;

import com.mojang.authlib.GameProfile;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.util.global.MojangApi;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

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
                    EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(MC.world, new GameProfile(playerUUID, player));
                    fakePlayer.copyLocationAndAnglesFrom(MC.player);

                    NBTTagCompound nbttagcompound = MC.player.writeToNBT(new NBTTagCompound());
                    fakePlayer.readFromNBT(nbttagcompound);

                    MC.world.addEntityToWorld(getRandomId(), fakePlayer);
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
