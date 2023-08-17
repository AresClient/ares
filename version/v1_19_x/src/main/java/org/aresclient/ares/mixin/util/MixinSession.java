package org.aresclient.ares.mixin.util;

import net.minecraft.client.util.Session;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Session.class)
public abstract class MixinSession implements org.aresclient.ares.api.minecraft.util.Session {
    @Override @Shadow public abstract String getSessionId();
    @Shadow @Final private String uuid;
    @Shadow @Final private String username;
    @Shadow @Final private String accessToken;

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }
}
