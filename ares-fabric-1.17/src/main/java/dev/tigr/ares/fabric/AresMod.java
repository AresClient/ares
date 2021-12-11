package dev.tigr.ares.fabric;

import dev.tigr.ares.core.Ares;
import dev.tigr.ares.core.feature.Command;
import dev.tigr.ares.core.feature.module.ClickGUIMod;
import dev.tigr.ares.core.feature.module.Module;
import dev.tigr.ares.fabric.impl.commands.*;
import dev.tigr.ares.fabric.impl.modules.combat.*;
import dev.tigr.ares.fabric.impl.modules.exploit.*;
import dev.tigr.ares.fabric.impl.modules.hud.elements.*;
import dev.tigr.ares.fabric.impl.modules.misc.*;
import dev.tigr.ares.fabric.impl.modules.movement.*;
import dev.tigr.ares.fabric.impl.modules.player.*;
import dev.tigr.ares.fabric.impl.modules.render.*;
import dev.tigr.ares.fabric.impl.render.CustomFontRenderer;
import dev.tigr.ares.fabric.impl.render.CustomRenderStack;
import dev.tigr.ares.fabric.impl.render.CustomRenderer;
import dev.tigr.ares.fabric.impl.render.CustomTextureManager;
import dev.tigr.ares.fabric.impl.util.CustomGUIManager;
import dev.tigr.ares.fabric.impl.util.CustomKeyboardManager;
import dev.tigr.ares.fabric.impl.util.CustomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tigermouthbear
 */
@Ares.Info(modLoader = "fabric", minecraftVersion = "1.17", version = "2.10", branch = Ares.Branches.BETA)
public class AresMod extends Ares {
    public AresMod() {
        UTILS = new CustomUtils();
        GUI_MANAGER = new CustomGUIManager();
        KEYBOARD_MANAGER = new CustomKeyboardManager();
        RENDERER = new CustomRenderer();
        RENDER_STACK = new CustomRenderStack();
        FONT_RENDERER = new CustomFontRenderer(MONO_FONT);
        TEXTURE_MANAGER = new CustomTextureManager();
    }

    @Override
    protected List<Class<? extends Module>> getModules() {
        // commented means that it still needs to be added
        List<Class<? extends Module>> modules = new ArrayList<>(Arrays.asList(
                // combat
                Anchor.class,
                AnchorAura.class,
                AntiBedAura.class,
                AutoArmor.class,
                AutoCity.class,
                AutoEz.class,
                AutoSurround.class,
                AutoTotem.class,
                AutoTrap.class,
                BedAura.class,
                BowRelease.class,
                Burrow.class,
                BurrowDetect.class,
                Criticals.class,
                CrystalAura.class,
                FireworkAura.class,
                HoleFiller.class,
                HopperAura.class,
                KillAura.class,
                Offhand.class,
                OffhandGap.class,
                SelfTrap.class,
                Surround.class,
                TotemPopCounter.class,

                // hud
                Armor.class,
                ChestCount.class,
                Coordinates.class,
                CrystalCount.class,
                ModuleList.class,
                InvPreview.class,
                LagNotifier.class,
                PlayerList.class,
                PlayerPreview.class,
                TotemCount.class,
                Watermark.class,
                Speedometer.class,
                TextShadow.class,

                // exploit
                AirInteract.class,
                FastPlace.class,
                InstantMine.class,
                LiquidInteract.class,
                MultiTask.class,
                NoBreakDelay.class,
                NoBreakReset.class,
                NoSwing.class,
                PacketCancel.class,
                PortalGodMode.class,
                SecretClose.class,
                ServerCrasher.class,
                SoundCoordLogger.class,

                // misc
                AntiGhostBlock.class,
                AutoTool.class,
                ChatSuffix.class,
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
                HighJump.class,
                IceSpeed.class,
                InventoryMove.class,
                Jesus.class,
                NoClip.class,
                NoSlowDown.class,
                PacketFly.class,
                SafeWalk.class,
                Speed.class,
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
                PacketMine.class,
                RotationLock.class,
                RotationManager.class,
                Scaffold.class,

                // render
                AntiOverlay.class,
                BlockHighlight.class,
                CameraClip.class,
                Capes.class,
                Chams.class,
                ChestESP.class,
                CustomFOV.class,
                DebugCrosshair.class,
                ESP.class,
                ExtraTab.class,
                FullBright.class,
                ViewModel.class,
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
        ));

        // only add discord rpc if we are on supported architecture
        String arch = System.getProperty("os.arch");
        if(arch.equals("i686") || arch.equals("i386")
                || arch.equals("x86") || arch.equals("x86_64")
                || arch.equals("amd64")) {
            modules.add(DiscordPresence.class);
        }

        return modules;
    }

    @Override
    protected List<Class<? extends Command>> getCommands() {
        return Arrays.asList(
                Bind.class,
                dev.tigr.ares.fabric.impl.commands.FakePlayer.class,
                Friend.class,
                Gamemode.class,
                Help.class,
                Load.class,
                Prefix.class,
                Save.class,
                Toggle.class,
                SearchCommand.class,
                Macros.class
        );
    }
}
