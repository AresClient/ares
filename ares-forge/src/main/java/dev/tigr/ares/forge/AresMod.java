package dev.tigr.ares.forge;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.forge.event.ForgeEvents;
import dev.tigr.ares.forge.impl.commands.*;
import dev.tigr.ares.forge.impl.modules.combat.*;
import dev.tigr.ares.forge.impl.modules.exploit.*;
import dev.tigr.ares.forge.impl.modules.hud.elements.*;
import dev.tigr.ares.forge.impl.modules.misc.*;
import dev.tigr.ares.forge.impl.modules.movement.*;
import dev.tigr.ares.forge.impl.modules.player.*;
import dev.tigr.ares.forge.impl.modules.render.*;
import dev.tigr.ares.forge.impl.render.CustomFontRenderer;
import dev.tigr.ares.forge.impl.render.CustomRenderStack;
import dev.tigr.ares.forge.impl.render.CustomRenderer;
import dev.tigr.ares.forge.impl.render.CustomTextureManager;
import dev.tigr.ares.forge.impl.util.CustomGUIManager;
import dev.tigr.ares.forge.impl.util.CustomKeyboardManager;
import dev.tigr.ares.forge.impl.util.CustomUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.opengl.Display;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tigermouthbear
 */
@Ares.Info(minecraftVersion = "forge", version = "2.9", branch = Ares.Branches.BETA)
public class AresMod extends Ares {
    @Mod(modid = Ares.MODID, name = Ares.NAME, clientSideOnly = true)
    public static final class Loader {
        @Mod.EventHandler
        public void init(FMLInitializationEvent event) {
            Ares.initialize(AresMod.class);
            Display.setTitle("Ares " + Ares.VERSION_FULL);
            MinecraftForge.EVENT_BUS.register(new ForgeEvents());
        }
    }

    public AresMod() {
        UTILS = new CustomUtils();
        GUI_MANAGER = new CustomGUIManager();
        KEYBOARD_MANAGER = new CustomKeyboardManager();
        RENDERER = new CustomRenderer();
        RENDER_STACK = new CustomRenderStack();
        FONT_RENDERER = new CustomFontRenderer("/assets/ares/font/font.ttf", 64);
        TEXTURE_MANAGER = new CustomTextureManager();
    }

    @Override
    protected List<Class<? extends Module>> getModules() {
        return Arrays.asList(
                // combat
                Anchor.class,
                AntiDeathScreen.class,
                Auto32k.class,
                AutoArmor.class,
                AutoCity.class,
                AutoEz.class,
                AutoSurround.class,
                AutoTotem.class,
                AutoTrap.class,
                BowRelease.class,
                Burrow.class,
                BurrowDetect.class,
                Criticals.class,
                CrystalAura.class,
                HoleFiller.class,
                HopperAura.class,
                KillAura.class,
                Offhand.class,
                OffhandGap.class,
                Surround.class,
                TotemPopCounter.class,

                // exploit
                CoordTpExploit.class,
                FastPlace.class,
                LiquidInteract.class,
                MultiTask.class,
                NewChunks.class,
                NoBreakDelay.class,
                NoBreakReset.class,
                NoSwing.class,
                PacketCancel.class,
                PortalGodMode.class,
                SecretClose.class,
                ServerCrasher.class,
                SoundCoordLogger.class,

                // hud
                Armor.class,
                ChestCount.class,
                Coordinates.class,
                CrystalCount.class,
                ModuleList.class,
                PlayerList.class,
                PlayerPreview.class,
                Speedometer.class,
                TotemCount.class,
                Watermark.class,
                TextShadow.class,
                InvPreview.class,

                // misc
                AutoTool.class,
                BetterSign.class,
                ChatSuffix.class,
                ConstantQMain.class,
                DiscordPresence.class,
                InfiniteChatLength.class,
                MidClickFriend.class,
                MsgOnToggle.class,
                PortalChat.class,
                ReloadSoundSystem.class,
                VisualRange.class,

                // movement
                AntiLevitation.class,
                AutoSprint.class,
                AutoWalk.class,
                Baritone.class,
                Blink.class,
                BoatFly.class,
                ElytraFly.class,
                EntitySpeed.class,
                Flight.class,
                IceSpeed.class,
                InventoryMove.class,
                Jesus.class,
                NoClip.class,
                NoSlowDown.class,
                PacketFly.class,
                SafeWalk.class,
                Strafe.class,
                Timer.class,
                Velocity.class,

                // player
                AntiHitbox.class,
                AntiHunger.class,
                HotbarReplenish.class,
                AutoReconnect.class,
                AutoSign.class,
                ClickGUIMod.class,
                FakeRotation.class,
                Freecam.class,
                NoForceLook.class,
                RotationLock.class,
                Scaffold.class,

                // render
                AntiOverlay.class,
                BlockHighlight.class,
                CameraClip.class,
                Capes.class,
                Chams.class,
                ChestESP.class,
                DebugCrosshair.class,
                ESP.class,
                ExtraTab.class,
                FullBright.class,
                HoleESP.class,
                MapTooltips.class,
                MobOwner.class,
                NameTags.class,
                NoArmorRender.class,
                NoFog.class,
                NoHurtShake.class,
                NoRender.class,
                NoWeather.class,
                Search.class,
                Tracers.class,
                Trajectories.class
        );
    }

    @Override
    protected List<Class<? extends Command>> getCommands() {
        return Arrays.asList(
                Bind.class,
                FakePlayer.class,
                Friend.class,
                Gamemode.class,
                Help.class,
                Load.class,
                Prefix.class,
                Save.class,
                SearchCommand.class,
                Toggle.class,
                Vanish.class,
                BaritoneCommand.class,
                Macros.class
        );
    }
}
