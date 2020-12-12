package dev.tigr.ares.fabric.event.client;

import net.minecraft.client.util.InputUtil;

public class SetKeyBindingStateEvent {
    private final InputUtil.Key keyBinding;
    private final boolean state;

    public SetKeyBindingStateEvent(InputUtil.Key keyBinding, boolean state) {
        this.keyBinding = keyBinding;
        this.state = state;
    }

    public InputUtil.Key getKeyBinding() {
        return keyBinding;
    }

    public boolean getState() {
        return state;
    }
}
