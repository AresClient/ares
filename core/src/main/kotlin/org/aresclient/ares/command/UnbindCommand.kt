package org.aresclient.ares.command

import org.aresclient.ares.Ares
import org.aresclient.ares.api.Keys
import java.util.*

object UnbindCommand: Command("Unbinds modules from keys", "unbind", "ub") {
    override fun execute(command: LinkedList<String>) {
        if(command.size == 1) {
            println("NO MODULE SPECIFIED")
            return
        }

        val moduleName = command[1]

        var successful = false
        for(module in Ares.MODULES) {
            if(module.name.contentEquals(moduleName, true)) {
                module.setBind(Keys.UNKNOWN)
                successful = true
            }
        }

        if(successful) {
            println("${moduleName.uppercase(Locale.getDefault())} SUCCESSFULLY UNBOUND")
        } else {
            println("NO SUCH MODULE")
        }
    }
}
