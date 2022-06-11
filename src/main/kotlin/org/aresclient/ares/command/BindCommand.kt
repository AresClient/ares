package org.aresclient.ares.command

import net.meshmc.mesh.util.Keys
import org.aresclient.ares.Ares
import org.aresclient.ares.Command
import java.util.*

object BindCommand: Command("Binds keys to modules", "bind", "kb", "keybind") {
    override fun execute(command: ArrayList<String>) {
        if(command.size == 1) {
            println("NO MODULE AND KEY SPECIFIED")
            return
        }

        if(command.size == 2) {
            println("NO KEY SPECIFIED")
            return
        }

        val moduleName = command[1]
        val keyName = command[2].uppercase(Locale.getDefault())
        val key = Keys.getKey(keyName)

        if(key == Keys.UNKNOWN) {
            println("BIND NOT VALID")
            return
        }

        var successful = false
        for(module in Ares.MODULES) {
            if(module.name.contentEquals(moduleName, true)) {
                module.setBind(key)
                successful = true
            }
        }

        if(successful)
            println("${moduleName.uppercase(Locale.getDefault())} SUCCESSFULLY BOUND TO ${Keys.getName(key)}")
        else
            println("NO SUCH MODULE")
    }
}