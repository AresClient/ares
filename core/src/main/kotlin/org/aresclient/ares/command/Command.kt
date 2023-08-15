package org.aresclient.ares.command

import org.aresclient.ares.Ares
import java.util.*

abstract class Command(val description: String, vararg names: String) {
    companion object {
        internal val MC = Ares.INSTANCE.minecraft
        val COMMANDS = hashMapOf<String, Command>()

        var prefix: Char = '-'

        fun processCommand(command: String) {
            // Split string where spaces separate words
            val parts = command.split(' ').toLinkedList()
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

            if(!COMMANDS.containsKey(parts[0].lowercase())) {
                println("COMMAND NOT FOUND")
                return
            }

            COMMANDS[parts.poll().lowercase()]?.execute(parts)
        }

        fun <T> Collection<T>.toLinkedList(): LinkedList<T> {
            return LinkedList(this)
        }
    }

    init {
        for(name in names) COMMANDS[name] = this
    }

    abstract fun execute(command: LinkedList<String>)

    protected fun outputText(string: String) {
        // TODO: Turn console prints into ingame visible chat messages or some rendered alternative
        println(string)
    }
}
