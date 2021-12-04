package dev.tigr.ares.fabric.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

import static dev.tigr.ares.Wrapper.MC;

public class CopiedOtherClientPlayerEntity extends OtherClientPlayerEntity {
    public CopiedOtherClientPlayerEntity(ClientWorld clientWorld, PlayerEntity playerEntity) {
        super(clientWorld, new GameProfile(UUID.randomUUID(), playerEntity.getGameProfile().getName()));

        copyFrom(playerEntity);
        getAttributes().setFrom(playerEntity.getAttributes());
        getInventory().clone(playerEntity.getInventory());

        dataTracker.set(PLAYER_MODEL_PARTS, MC.player.getDataTracker().get(PLAYER_MODEL_PARTS));
    }
}
