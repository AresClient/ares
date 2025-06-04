package org.aresclient.ares.impl

import net.fabricmc.loader.api.FabricLoader
import org.aresclient.ares.api.Plugin
import org.aresclient.ares.impl.instrument.commands.*
import org.aresclient.ares.impl.instrument.globals.Camera
import org.aresclient.ares.impl.instrument.globals.Interaction
import org.aresclient.ares.impl.instrument.globals.Rotation
import org.aresclient.ares.impl.instrument.modules.hud.Coordinates
import org.aresclient.ares.impl.instrument.modules.hud.ModuleList
import org.aresclient.ares.impl.instrument.modules.hud.ToggleList
import org.aresclient.ares.impl.instrument.modules.hud.Watermark
import org.aresclient.ares.impl.instrument.modules.misc.ClickGUI
import org.aresclient.ares.impl.instrument.modules.misc.TitleScreen
import org.aresclient.ares.impl.instrument.modules.misc.ToggleNotifications
import org.aresclient.ares.impl.instrument.modules.movement.AutoWalk
import org.aresclient.ares.impl.instrument.modules.movement.Baritone
import org.aresclient.ares.impl.instrument.modules.movement.SafeWalk
import org.aresclient.ares.impl.instrument.modules.movement.speed.Speed
import org.aresclient.ares.impl.instrument.modules.offence.autocrystal.AutoCrystal
import org.aresclient.ares.impl.instrument.modules.player.*
import org.aresclient.ares.impl.instrument.modules.render.*
import org.aresclient.ares.impl.instrument.modules.render.esp.ESP

object AresPlugin: Plugin(
    "Ares",
    "The main Ares utility mod plugin",
    "3.0-SNAPSHOT",
    "1.21.5",
    arrayOf("Tigermouthbear", "Makrennel"),
    globals = listOf(
        Camera,
        Interaction,
        Rotation
    ),
    modules = listOfNotNull(
        Coordinates,
        ModuleList,
        ToggleList,
        Watermark,

        ClickGUI,
        ToggleNotifications,
        TitleScreen,

        AutoWalk,
        if(FabricLoader.getInstance().isModLoaded("baritone")) Baritone else null,
        SafeWalk,
        Speed,

        AutoCrystal,

        AntiAFK,
        FastPlace,
        MultiTask,
        PortalGUIs,
        Sync,

        BlockEntityESP,
        CameraClip,
        DiamondSearchExample,
        ESP,
        Freecam,
        Fullbright,
        NoRender,
        Tracers
    ),
    commands = listOf(
        EchoCommand,
        HelpCommand,
        SaveCommand,
        LoadCommand,
        SetCommand,
        ResetCommand,
        GetCommand,
        ToggleCommand,
        PrefixCommand,
        FriendCommand,
        ClearCommand,
        FakePlayerCommand,
        HudCommand
    )
)