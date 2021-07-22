package dev.tigr.ares.fabric.mixin.accessors;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.CommandSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatScreen.class)
public interface ChatScreenAccessor {
    @Accessor("commandSuggestor")
    CommandSuggestor getCommandSuggestor();

    @Accessor("originalChatText")
    String getOriginalChatText();
}
