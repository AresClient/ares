package org.aresclient.ares.impl.iface

import org.aresclient.ares.api.command.Command

// TODO: print to chat
class CommandContext: Command.IContext {
    override fun print(message: String?) {
        println(message)
    }

    override fun error(message: String?) {
        println("error: $message")
    }
}