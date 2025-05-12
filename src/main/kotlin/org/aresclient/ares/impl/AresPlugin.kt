package org.aresclient.ares.impl

import org.aresclient.ares.api.Plugin
import org.aresclient.ares.impl.instrument.commands.*
import org.aresclient.ares.impl.instrument.global.*
import org.aresclient.ares.impl.instrument.module.modules.misc.ClickGUI
import org.aresclient.ares.impl.instrument.module.modules.misc.ToggleNotifications
import org.aresclient.ares.impl.instrument.module.modules.misc.TitleScreen
import org.aresclient.ares.impl.instrument.module.modules.movement.SafeWalk
import org.aresclient.ares.impl.instrument.module.modules.movement.Speed
import org.aresclient.ares.impl.instrument.module.modules.offence.AutoCrystal
import org.aresclient.ares.impl.instrument.module.modules.player.AntiAFK
import org.aresclient.ares.impl.instrument.module.modules.player.Freecam
import org.aresclient.ares.impl.instrument.module.modules.player.PortalGUIs
import org.aresclient.ares.impl.instrument.module.modules.render.*

object AresPlugin: Plugin(
    "Ares",
    "The main Ares utility mod plugin",
    "3.0.0-SNAPSHOT",
    "1.21.5",
    arrayOf("Tigermouthbear", "Makrennel"),
    globals = listOf(
        Camera,
        Interaction,
        Rotation
    ),
    modules = listOf(
        ClickGUI,
        ToggleNotifications,
        TitleScreen,

        SafeWalk,
        Speed,

        AutoCrystal,

        AntiAFK,
        Freecam,
        PortalGUIs,

        BlockEntityESP,
        DiamondSearchExample,
        ESP,
        Fullbright,
        NoRender,
        TestModule,
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
        FakePlayerCommand
    )
)