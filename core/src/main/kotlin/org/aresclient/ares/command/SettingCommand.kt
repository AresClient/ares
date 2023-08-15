package org.aresclient.ares.command

import org.aresclient.ares.*
import org.aresclient.ares.renderer.Color
import java.util.*

/**
 * Spaces are used to separate parts of a command
 * Names which have spaces are separated by underscores and then later changed to spaces when comparing
 * Values in lists/arrays are separated by commas - Color / List settings
 */
object SettingCommand: Command("Used to get or set the value for settings", "setting", "set", "settings") {
    override fun execute(command: LinkedList<String>) {
        if(command.size == 0) {
            outputText("SETTING COMMAND: NOTHING SPECIFIED")
            return
        }

        when (command.poll()) {
            "list","l","ls" -> listArg(command)
            "get","g" -> getArg(command)
            "set","s" -> setArg(command)
            "help","h" -> helpArg(command)
            else -> {
                outputText("SETTING COMMAND: ARGUMENT NOT FOUND")
                outputText("Enter \"-setting help\" for information on how to use this command")
                return
            }
        }
    }
    
    private fun helpArg(command: LinkedList<String>) {
        outputText("=======================")
        outputText("Setting Command Help:")
        outputText("=======================")
        outputText("Valid Arguments:")
        outputText("_______________________")
        outputText(" l")
        outputText(" ls")
        outputText(" list - Lists the settings or paths that are contained in the specified path")
        outputText(" -- syntax: -setting list path/to_list")
        outputText("_______________________")
        outputText(" g")
        outputText(" get - Returns the current value and description of a setting or path")
        outputText(" -- syntax: -setting get path_to/setting")
        outputText("_______________________")
        outputText(" s")
        outputText(" set - Sets the current value of a setting to a different value")
        outputText(" -- syntax (normal): -setting set path_to/setting value")
        outputText(" -- syntax (color): -setting set path_to/setting r,g,b,a")
        outputText(" -- syntax (list): -setting set path_to/setting add/remove/replace v,v,v...")
        outputText("_______________________")
        outputText(" h")
        outputText(" help - Prints this dialogue")
        outputText("=======================")
        
    }

    private fun processPath(path: String): Serializable? {
        // Split the path into individual names
        val parts = path.split('/').toLinkedList()

        // Replace underscores with spaces - paths are given with underscores where spaces are found in the name
        for ((index, s) in parts.withIndex()) {
            parts[index] = s.replace('_',' ')
        }

        // Get to the bottom of the given path, or return null if impossible
        var current: Serializable? = Ares.SETTINGS
        while (parts.size != 0) {
            if (current !is Settings) {
                if (current != null) outputText("SETTING COMMAND: ${current.getName()} IS NOT A PATH")
                if (current == null) outputText("SETTING COMMAND: NOT A VALID PATH")
                return null
            }
            current = nextSerializable(parts.poll(), current)
        }

        if (current == null) outputText("SETTING COMMAND: NOT A VALID PATH")
        return current
    }

    private fun nextSerializable(next: String, last: Settings): Serializable? {
        // Search for the correct key and return its value
        for (entry in last.getMap().entries) {
            if (entry.key.contentEquals(next, true)) {
                return entry.value
            }
        }

        return null
    }

    // Prints a list of all serializable types in a path
    private fun listArg(command: LinkedList<String>) {
        val serializable = processPath(command.poll()) ?: return

        // Obviously, can't list from a Setting
        if (serializable is Setting<*>) {
            outputText("SETTING COMMAND: ${serializable.getFullName()} IS A SETTING, NOT A PATH")
            return
        }

        // List each value with what the value does
        outputText("${serializable.getFullName()} Contents:")
        for (entry in (serializable as Settings).getMap().entries) {
            val type = if (entry.value is Setting<*>) (entry.value as Setting<*>).type.name else "PATH"
            val subVal = if (entry.value is Setting<*>) " : ${(entry.value as Setting<*>).value.toString()}" else " ===>"
            outputText("$type : ${entry.key} $subVal")
        }
    }

    // Get a specific serializable and print details about it
    private fun getArg(command: LinkedList<String>) {
        val serializable = processPath(command.poll()) ?: return

        outputText("${serializable.getName()} Details:")
        if (serializable.getParent() != null) outputText("Parent: ${serializable.getParent()!!.getName()}")

        // Print a description for the serializable
        // TODO: Description

        // Nothing more to do for categories
        if (serializable !is Setting<*>) return

        // Print setting type specific details
        outputText("Current Value: ${serializable.value.toString()}")
        when (serializable.type) {
            Setting.Type.INTEGER, Setting.Type.DOUBLE, Setting.Type.FLOAT, Setting.Type.LONG -> {
                serializable.possibleValues as RangeValues
                outputText("Minimum: ${serializable.possibleValues.min ?: "No Limit"}")
                outputText("Maximum: ${serializable.possibleValues.max ?: "No Limit"}")
            }
            Setting.Type.ENUM -> {
                outputText("Possible Values:")
                serializable as Setting<Enum<*>>
                for (opt in serializable.value.javaClass.enumConstants) outputText("${opt.ordinal} - ${opt.name}")
            }
            Setting.Type.LIST -> {
                // Because of how many values lists can have, do not show them all by default
                if (command.poll().contentEquals("SHOW_VALUES", true)) {
                    outputText("Possible Values:")
                    serializable.possibleValues as ListValues<*>
                    for (opt in serializable.possibleValues.values) outputText("--- ${opt.toString()}")
                }
                else outputText("To see a list of available values, repeat this command and add SHOW_VALUES as the final argument")
            }
            else -> Unit
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setArg(command: LinkedList<String>) {
        val serializable = processPath(command.poll())

        if (serializable !is Setting<*>) {
            outputText("SETTING COMMAND: ADDRESS DOES NOT POINT TO A SETTING")
            return
        }

        val oldVal = serializable.value.toString()
        val newVal = command.poll()

        when (serializable.type) {
            Setting.Type.STRING -> (serializable as Setting<String>).value = newVal
            Setting.Type.BOOLEAN -> {
                serializable as Setting<Boolean>
                if (newVal.contentEquals("T", true) || newVal.contentEquals("true", true)) serializable.value = true
                else if (newVal.contentEquals("F", true) || newVal.contentEquals("false", true)) serializable.value = false
                else {
                    outputText("SETTING COMMAND: $newVal IS NOT A VALID VALUE FOR BOOLEAN")
                    return
                }
            }
            Setting.Type.ENUM -> {
                serializable as Setting<Enum<*>>
                val enumConstants = serializable.value.javaClass.enumConstants
                var success = false
                val ordinal = newVal.toIntOrNull()
                if (ordinal == null) {
                    for (value in enumConstants)
                        if (value.toString().contentEquals(newVal, true)) {
                            serializable.value = value
                            success = true
                        }
                }
                else if (ordinal <= enumConstants.size - 1 && ordinal > -1) {
                    serializable.value = enumConstants[ordinal]
                    success = true
                }

                if (!success) {
                    outputText("SETTING COMMAND: $newVal IS NOT A VALID VALUE FOR THIS ${serializable.getName()}")
                    return
                }
            }
            Setting.Type.COLOR -> {
                serializable as Setting<Color>

                val rgba = newVal.split(',')
                if(rgba.size != 4) {
                    outputText("SETTING COMMAND: ONLY ${rgba.size} VALUES WERE GIVEN AND FOUR ARE REQUIRED")
                    return
                }

                val values = floatArrayOf(0F,0F,0F,0F)
                for(i in 0 until 4) {
                    val v = rgba[i].toFloatOrNull()
                    if (v == null) {
                        outputText("SETTING COMMAND: AN INVALID VALUE WAS GIVEN")
                        return
                    }
                    values[i] = v
                }

                serializable.value = Color(values[0], values[1], values[2], values[3])
            }
            Setting.Type.INTEGER -> {
                val int = newVal.toIntOrNull()
                if(int != null) {
                    serializable as Setting<Int>
                    if(!(serializable.possibleValues as RangeValues).noBounds() && (int > serializable.possibleValues.max!! || int < serializable.possibleValues.min!!)) {
                        outputText("SETTING COMMAND: CANNOT SET ${serializable.getName()} TO $int: VALUE IS OUT OF BOUNDS")
                        return
                    }
                    serializable.value = int
                }
            }
            Setting.Type.DOUBLE -> {
                val double = newVal.toDoubleOrNull()
                if(double != null) {
                    serializable as Setting<Double>
                    if(!(serializable.possibleValues as RangeValues).noBounds() && (double > serializable.possibleValues.max!! || double < serializable.possibleValues.min!!)) {
                        outputText("SETTING COMMAND: CANNOT SET ${serializable.getName()} TO $double: VALUE IS OUT OF BOUNDS")
                        return
                    }
                    serializable.value = double
                }
            }
            Setting.Type.FLOAT -> {
                val float = newVal.toFloatOrNull()
                if(float != null) {
                    serializable as Setting<Float>
                    if(!(serializable.possibleValues as RangeValues).noBounds() && (float > serializable.possibleValues.max!! || float < serializable.possibleValues.min!!)) {
                        outputText("SETTING COMMAND: CANNOT SET ${serializable.getName()} TO $float: VALUE IS OUT OF BOUNDS")
                        return
                    }
                    serializable.value = float
                }
            }
            Setting.Type.LONG -> {
                val long = newVal.toLongOrNull()
                if(long != null) {
                    serializable as Setting<Long>
                    if(!(serializable.possibleValues as RangeValues).noBounds() && (long > serializable.possibleValues.max!! || long < serializable.possibleValues.min!!)) {
                        outputText("SETTING COMMAND: CANNOT SET ${serializable.getName()} TO $long: VALUE IS OUT OF BOUNDS")
                        return
                    }
                    serializable.value = long
                }
            }
            Setting.Type.LIST -> {
                val list = command.poll().split(',')
                serializable.possibleValues as ListValues<*>

                // Is it a list of enums or a list of strings
                val type: Int
                val setting =
                    if(serializable.possibleValues.values[0] is String) {
                        type = 0
                        serializable.possibleValues as ListValues<String>
                    }
                    else {
                        type = 1
                        serializable.possibleValues as ListValues<Enum<*>>
                    }

                // Add values to list
                if(newVal.contentEquals("add", true)) {
                    // Create a mutable copy of the current list
                    val newList = (serializable.value as List<*>).toMutableList()
                    
                    // String List
                    if(type == 0) {
                        list.forEach {
                            if(setting.values.contains(it) && !newList.contains(it)) newList.add(it)
                        }
                    }

                    //Enum List
                    else {
                        for(str in list) {
                            var enum: Enum<*>? = null
                            setting.values[0].javaClass.enumConstants.forEach {
                                if((it as Enum<*>).name.contentEquals(str, true)) enum = it
                            }

                            if(enum == null) continue
                            if(!newList.contains(enum)) newList.add(enum)
                        }
                    }

                    // Set serializable to new list
                    (serializable as Setting<List<*>>).value = newList
                }

                // Remove values from list
                else if(newVal.contentEquals("remove", true)) {
                    // Create a mutable copy of the current list
                    val newList = (serializable.value as List<*>).toMutableList()
                    
                    // String List
                    if(type == 0) {
                        list.forEach {
                            if(newList.contains(it)) newList.remove(it)
                        }
                    }

                    // Enum List
                    else {
                        for(str in list) {
                            var enum: Enum<*>? = null
                            setting.values[0].javaClass.enumConstants.forEach {
                                if((it as Enum<*>).name.contentEquals(str, true)) enum = it
                            }

                            if(enum == null) continue
                            if(newList.contains(enum!!)) newList.remove(enum!!)
                        }
                    }

                    // Set serializable to new list
                    (serializable as Setting<List<*>>).value = newList
                }

                // Replace the current list with a new list of values
                else if(newVal.contentEquals("replace", true)) {
                    // String List
                    if(type == 0) {
                        val newList = LinkedList<String>()
                        list.forEach {
                            if(setting.values.contains(it)) newList.add(it)
                        }

                        (serializable as Setting<List<String>>).value = newList
                    }

                    // Enum list
                    else {
                        val newList = LinkedList<Enum<*>>()
                        for(str in list) {
                            var enum: Enum<*>? = null
                            setting.values[0].javaClass.enumConstants.forEach {
                                if((it as Enum<*>).name.contentEquals(str, true)) enum = it
                            }

                            if(enum == null) continue
                            newList.add(enum!!)
                        }

                        (serializable as Setting<List<Enum<*>>>).value = newList
                    }
                }

                else {
                    outputText("SETTING COMMAND: A VALID COMMAND WAS NOT GIVEN TO SET ${serializable.getName()}")
                    outputText("Valid Commands:")
                    outputText(" add     - Adds values to the current list")
                    outputText(" remove  - Removes values from the current list")
                    outputText(" replace - Replaces the current list with a new list of values")
                    return
                }
            }
            Setting.Type.ARRAY -> TODO()
            Setting.Type.BIND -> TODO()
        }

        outputText("Successfully applied new value to ${serializable.getName()}!")
        outputText("Old Value: $oldVal")
        outputText("New Value: ${serializable.value.toString()}")
    }
}
