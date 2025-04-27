package org.aresclient.ares.impl.util

import net.minecraft.text.Text
import org.aresclient.ares.api.Wrapper
import org.aresclient.ares.api.instruments.Command
import org.aresclient.ares.api.render.TextColor

object ChatUtil: Command.IContext, Wrapper {
    override fun print(message: String) {
        MC.inGameHud.chatHud.addMessage(Text.of("${TextColor.DARK_GRAY}[${TextColor.DARK_RED}Ares${TextColor.DARK_GRAY}] ${TextColor.WHITE}$message"))
    }

    override fun error(message: String) {
        MC.inGameHud.chatHud.addMessage(Text.of("${TextColor.DARK_GRAY}[${TextColor.DARK_RED}Ares${TextColor.DARK_GRAY}] ${TextColor.RED}$message"))
    }
}
