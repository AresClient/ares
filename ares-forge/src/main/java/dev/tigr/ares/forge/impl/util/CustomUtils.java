package dev.tigr.ares.forge.impl.util;

import com.mojang.authlib.exceptions.AuthenticationException;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.client.SystemChatMessageEvent;
import dev.tigr.ares.core.util.AbstractAccount;
import dev.tigr.ares.core.util.IUtils;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.forge.impl.modules.hud.EditHudGui;
import dev.tigr.ares.forge.impl.modules.movement.Baritone;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

import static dev.tigr.ares.Wrapper.MC;

/**
 * @author Tigermouthbear 11/23/20
 */
public class CustomUtils implements IUtils {
    @Override
    public void printMessage(String message) {
        TextComponentString textComponentString = new TextComponentString(message);

        Ares.EVENT_MANAGER.post(new SystemChatMessageEvent(textComponentString.getFormattedText()));

        ITextComponent text;
        text = new TextComponentString(TextColor.DARK_GRAY + "[" + TextColor.DARK_RED + "Ares" + TextColor.DARK_GRAY + "]" + TextColor.WHITE + " ").appendSibling(textComponentString);

        MC.ingameGUI.addChatMessage(ChatType.SYSTEM, text);
    }

    @Override
    public void executeBaritoneCommand(String string) {
        Baritone.executeCommand(string);
    }

    @Override
    public String getPlayerName() {
        return MC.getSession().getUsername();
    }

    @Override
    public void openHUDEditor() {
        MC.addScheduledTask(() -> MC.displayGuiScreen(new EditHudGui(MC.currentScreen)));
    }

    @Override
    public AbstractAccount createAccount(String email, String password, String uuid) throws IOException {
        return new CustomAccount(email, password, uuid);
    }

    @Override
    public AbstractAccount createAccount(String email, String password) throws IOException, AuthenticationException {
        return new CustomAccount(email, password);
    }

    @Override
    public void shutdown() {
        MC.shutdown();
    }
}
