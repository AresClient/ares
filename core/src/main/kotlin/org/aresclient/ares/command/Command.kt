package org.aresclient.ares.command

import org.aresclient.ares.Ares
import java.util.*

/**
 * TODO: Turn console prints into ingame visible chat messages or some rendered alternative
 */
abstract class Command(val description: String, vararg names: String) {
    companion object {
        internal val MC = Ares.INSTANCE.minecraft
        val COMMANDS = hashMapOf<String, Command>()

        var prefix: Char = '-'

        fun processCommand(command: String) {
            // Split string where spaces separate words
            val parts = command.split(' ').toMutableList()
            parts.removeIf { it.isEmpty() }

            // Remove prefix
            if(parts[0].length == 1) {
                if(parts.size == 1) {
                    println("NO COMMAND SPECIFIED")
                    return
                }
                parts.removeFirst()
            }
            else parts[0] = parts[0].substring(1)

            if(!Command.COMMANDS.containsKey(parts[0])) {
                println("COMMAND NOT FOUND")
                return
            }

            COMMANDS[parts[0].lowercase(Locale.getDefault())]?.execute(parts as ArrayList<String>)
        }
    }

    init {
        for(name in names) COMMANDS[name] = this
    }

    abstract fun execute(command: ArrayList<String>)
}
