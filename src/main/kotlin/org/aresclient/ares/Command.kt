package org.aresclient.ares

import java.util.*

/**
 * TODO: Turn console prints into chat messages or some rendered alternative
 */
abstract class Command(val name: String, val description: String) {
    companion object {
        val MC = Ares.MESH.minecraft

        val COMMANDS = hashMapOf<String, Command>()
        var prefix: Char = '-'

        fun processCommand(command: String) {
            //Split string where spaces separate words
            val parts = command.split(' ').toMutableList()

            for(part in parts) if(part.isEmpty()) parts.remove(part)

            // Remove prefix
            if(parts[0].length == 1) {
                if(parts.size == 1) {
                    println("NO COMMAND SPECIFIED")
                    return
                }
                parts.removeFirst()
            }
            else parts[0] = parts[0].substring(1)

            if(!COMMANDS.containsKey(parts[0])) {
                println("COMMAND NOT FOUND")
                return
            }

            COMMANDS[parts[0].lowercase(Locale.getDefault())]?.execute(parts)
        }
    }

    init {
        COMMANDS[name] = this
    }

    abstract fun execute(command: List<String>)
}