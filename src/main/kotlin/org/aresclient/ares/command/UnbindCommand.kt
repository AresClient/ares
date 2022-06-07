package org.aresclient.ares.command

import net.meshmc.mesh.util.Keys
import org.aresclient.ares.Ares
import org.aresclient.ares.Command
import java.util.*

object UnbindCommand: Command("unbind", "Unbinds modules from keys") {
    init {
        COMMANDS["ub"] = this
    }

    override fun execute(command: ArrayList<String>) {
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