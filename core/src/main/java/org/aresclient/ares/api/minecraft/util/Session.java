package org.aresclient.ares.api.minecraft.util;

public interface Session {
    String getSessionId();
    String getUuid();
    String getUsername();
    String getAccessToken();
}
