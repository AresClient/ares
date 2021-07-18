package dev.tigr.ares.fabric.impl.modules.misc;

import com.mojang.authlib.GameProfile;
import dev.tigr.ares.core.feature.module.Category;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.core.setting.Setting;
import dev.tigr.ares.core.setting.settings.StringSetting;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

@Module.Info(name = "FakePlayer", description = "Makes a fake player entity that modules can be tested using.", category = Category.MISC)
public class FakePlayer extends Module {
    int id;

    private final Setting<String> name = register(new StringSetting("Name", "Ares"));

    @Override
    public void onEnable() {
        OtherClientPlayerEntity fakePlayer = new OtherClientPlayerEntity(MC.world, new GameProfile(UUID.randomUUID(), name.getValue()));
        fakePlayer.copyFrom(MC.player);

        NbtCompound compoundTag = new NbtCompound();
        MC.player.writeCustomDataToNbt(compoundTag);
        fakePlayer.readCustomDataFromNbt(compoundTag);

        MC.world.addEntity(fakePlayer.getId(), fakePlayer);

        id = fakePlayer.getId();
    }

    @Override
    public void onDisable() {
        MC.world.removeEntity(id, Entity.RemovalReason.DISCARDED);
    }
}
