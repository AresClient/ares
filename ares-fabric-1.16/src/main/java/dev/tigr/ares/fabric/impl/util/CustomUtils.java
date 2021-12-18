package dev.tigr.ares.fabric.impl.util;

import com.mojang.authlib.exceptions.AuthenticationException;
import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.event.client.SystemChatMessageEvent;
import dev.tigr.ares.core.util.AbstractAccount;
import dev.tigr.ares.core.util.IUtils;
import dev.tigr.ares.core.util.render.TextColor;
import dev.tigr.ares.fabric.impl.modules.hud.EditHudGui;
import dev.tigr.ares.fabric.impl.modules.movement.Baritone;
import dev.tigr.ares.fabric.mixin.accessors.MinecraftClientAccessor;
import dev.tigr.ares.fabric.mixin.accessors.RenderTickCounterAccessor;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.options.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.io.IOException;

import static dev.tigr.ares.Wrapper.MC;

/**
 * @author Tigermouthbear 11/23/20
 */
public class CustomUtils implements IUtils {
    @Override
    public void printMessage(String message) {
        Text textComponentString = new LiteralText(message);
        Ares.EVENT_MANAGER.post(new SystemChatMessageEvent(textComponentString.getString()));
        MC.inGameHud.getChatHud().addMessage(new LiteralText(TextColor.DARK_GRAY + "[" + TextColor.DARK_RED + "Ares" + TextColor.DARK_GRAY + "]" + TextColor.WHITE + " ").append(textComponentString));
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
        MC.openScreen(new EditHudGui(MC.currentScreen));
    }

    @Override
    public void openTitleScreen() {
        MC.openScreen(new TitleScreen());
    }

    @Override
    public void openSinglePlayerMenu() {
        MC.openScreen(new SelectWorldScreen(MC.currentScreen));
    }

    @Override
    public void openMultiPlayerMenu() {
        MC.openScreen(new MultiplayerScreen(MC.currentScreen));
    }

    @Override
    public void openRealmsMenu() {
        MC.openScreen(new RealmsMainScreen(MC.currentScreen));
    }

    @Override
    public void openOptionsMenu() {
        MC.openScreen(new OptionsScreen(MC.currentScreen, MC.options));
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
        MC.scheduleStop();
    }

    @Override
    public float getRenderPartialTicks() {
        return MC.getTickDelta();
    }

    @Override
    public float getTickLength() {
        return ((RenderTickCounterAccessor) ((MinecraftClientAccessor) MC).getRenderTickCounter()).getTickTime();
    }

    @Override
    public void setTickLength(float tickLength) {
        ((RenderTickCounterAccessor) ((MinecraftClientAccessor) MC).getRenderTickCounter()).setTickTime(tickLength);
    }
}
