package org.aresclient.ares.command

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.ListValues
import org.aresclient.ares.RangeValues
import org.aresclient.ares.global.Global
import org.aresclient.ares.Setting
import org.aresclient.ares.Settings
import org.aresclient.ares.module.Module
import java.util.*
import kotlin.collections.ArrayList

/**
 * Spaces are used to separate parts of a command
 * Names which have spaces are separated by underscores and then later changed to spaces when comparing
 * Values in lists/arrays are separated by commas - Color / List settings
 */
object SettingCommand: Command("Used to get or set the value for settings", "setting", "set", "settings") {
    override fun execute(command: ArrayList<String>) {
        if(command.size == 1) {
            println("NOTHING SPECIFIED")
            return
        }

        if(command.size == 2) {
            println("TARGET AND SETTING NOT SPECIFIED")
            return
        }

        for(i in 2 until command.size)
            command[i] = command[i].replace('_', ' ').uppercase(Locale.getDefault())

        val setting =
            if(command[2].endsWith("GLOBAL"))
                getSetting(command, Global.SETTINGS)
            else
                getSetting(command, Module.SETTINGS)

        if(setting == null) return

        if(command[1].startsWith("g")) get(setting)

        val sVal =
            if(setting.type == Setting.Type.LIST)
                arrayOf(command[command.size -2], command[command.size -1])
            else
                arrayOf(command[command.size -1])

        if(command[1].startsWith("s")) set(setting, sVal)
    }

    private fun getSetting(command: ArrayList<String>, settings: Settings): Setting<*>? {
        var i = 2
        var current = settings.get(command[i])

        if(current == null) {
            println("COMMAND INDEX OF 2 NOT FOUND")
            return null
        }

        // to account for nested categories
        while(current is Settings) {
            if(++i == command.size) {
                println("NO SETTING SPECIFIED")
                return null
            }

            current = current.get(command[i])
            if(current == null) {
                println("COMMAND INDEX OF $i NOT FOUND")
                return null
            }
        }

        return current as Setting<*>
    }

    private fun get(setting: Setting<*>) {
        println(setting.value.toString())
    }

    @Suppress("UNCHECKED_CAST")
    private fun set(setting: Setting<*>, values: Array<String>) {
        val value = values[0]
        when(setting.type) {
            Setting.Type.STRING -> (setting as Setting<String>).value = value
            Setting.Type.BOOLEAN -> {
                val b =
                    if(value.startsWith('T')) true
                    else if(value.startsWith('F')) false
                    else null
                if(b != null) {
                    (setting as Setting<Boolean>).value = b
                    println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO ${setting.value.toString().uppercase(Locale.getDefault())}")
                }
            }
            Setting.Type.ENUM -> {
                val ordinal = value.toIntOrNull()
                if(ordinal != null) {
                    val array = (setting as Setting<Enum<*>>).value.javaClass.enumConstants
                    if(ordinal <= array.size - 1 && ordinal > -1) {
                        setting.value = array[ordinal]
                        println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO ${setting.value.name}")
                    }
                }
                else {
                    val valEnum = value.replace(' ', '_')
                    val enumConstants = (setting as Setting<Enum<*>>).value.javaClass.enumConstants ?: return
                    for(v in enumConstants)
                        if(v.toString().contentEquals(valEnum, true)) {
                            setting.value = v
                            println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO ${setting.value.name}")
                        }
                }
            }
            Setting.Type.COLOR -> {
                val s = value.split(',')
                if(s.size != 4) return

                val values = floatArrayOf(0F,0F,0F,0F)
                for(i in 0 until 4) {
                    val temp = s[i].toFloatOrNull() ?: return
                    values[i] = temp
                }

                (setting as Setting<Color>).value = Color(values[0], values[1], values[2], values[3])
                println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO ${setting.value.red},${setting.value.green},${setting.value.blue},${setting.value.alpha}")
            }
            Setting.Type.INTEGER -> {
                val int = value.toIntOrNull()
                if(int != null) {
                    setting as Setting<Int>
                    if(!(setting.possibleValues as RangeValues).noBounds() && (int > setting.possibleValues.max!! || int < setting.possibleValues.min!!)) {
                        println("CANNOT SET ${setting.name.uppercase(Locale.getDefault())} TO $int: VALUE IS OUT OF BOUNDS")
                        return
                    }
                    setting.value = int
                    println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO $int")
                }
            }
            Setting.Type.DOUBLE -> {
                val double = value.toDoubleOrNull()
                if(double != null) {
                    setting as Setting<Double>
                    if(!(setting.possibleValues as RangeValues).noBounds() && (double > setting.possibleValues.max!! || double < setting.possibleValues.min!!)) {
                        println("CANNOT SET ${setting.name.uppercase(Locale.getDefault())} TO $double: VALUE IS OUT OF BOUNDS")
                        return
                    }
                    setting.value = double
                    println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO $double")
                }
            }
            Setting.Type.FLOAT -> {
                val float = value.toFloatOrNull()
                if(float != null) {
                    setting as Setting<Float>
                    if(!(setting.possibleValues as RangeValues).noBounds() && (float > setting.possibleValues.max!! || float < setting.possibleValues.min!!)) {
                        println("CANNOT SET ${setting.name.uppercase(Locale.getDefault())} TO $float: VALUE IS OUT OF BOUNDS")
                        return
                    }
                    setting.value = float
                    println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO $float")
                }
            }
            Setting.Type.LONG -> {
                val long = value.toLongOrNull()
                if(long != null) {
                    setting as Setting<Long>
                    if(!(setting.possibleValues as RangeValues).noBounds() && (long > setting.possibleValues.max!! || long < setting.possibleValues.min!!)) {
                        println("CANNOT SET ${setting.name.uppercase(Locale.getDefault())} TO $long: VALUE IS OUT OF BOUNDS")
                        return
                    }
                    setting.value = long
                    println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO $long")
                }
            }
            Setting.Type.LIST -> {
                val list = values[1].split(",")
                setting.possibleValues as ListValues<*>
                val type: Int
                val sett =
                    if(setting.possibleValues.values[0] is String) {
                        type = 0
                        setting.possibleValues as ListValues<String>
                    }
                    else {
                        type = 1
                        setting.possibleValues as ListValues<Enum<*>>
                    }

                if(value == "ADD") {
                    val newList = (setting.value as List<*>).toMutableList()
                    if(type == 0) {
                        list.forEach {
                            if(sett.values.contains(it) && !newList.contains(it)) newList.add(it)
                        }
                    }

                    else {
                        for(str in list) {
                            var enum: Enum<*>? = null
                            sett.values[0].javaClass.enumConstants.forEach {
                                if((it as Enum<*>).name.uppercase(Locale.getDefault()) == str.uppercase(Locale.getDefault())) enum = it
                            }

                            if(enum == null) continue
                            if(!newList.contains(enum)) newList.add(enum)
                        }
                    }

                    (setting as Setting<List<*>>).value = newList
                    println("ATTEMPTED TO ADD ${values[1]} TO LIST ${setting.name.uppercase(Locale.getDefault())}: USE \"GET\" TO CHECK")
                }

                else if(value == "REMOVE") {
                    val newList = (setting.value as List<*>).toMutableList()
                    if(type == 0) {
                        list.forEach {
                            if(newList.contains(it)) newList.remove(it)
                        }
                    }

                    else {
                        for(str in list) {
                            var enum: Enum<*>? = null
                            sett.values[0].javaClass.enumConstants.forEach {
                                if((it as Enum<*>).name.uppercase(Locale.getDefault()) == str.uppercase(Locale.getDefault())) enum = it
                            }

                            if(enum == null) continue
                            if(newList.contains(enum!!)) newList.remove(enum!!)
                        }
                    }

                    (setting as Setting<List<*>>).value = newList

                    println("ATTEMPTED TO REMOVE ${values[1]} FROM LIST ${setting.name.uppercase(Locale.getDefault())}: USE \"GET\" TO CHECK")
                }

                else if(value == "REPLACE") {
                    if(type == 0) {
                        val newList = ArrayList<String>()
                        list.forEach {
                            if(sett.values.contains(it)) newList.add(it)
                        }

                        (setting as Setting<List<String>>).value = newList
                    }

                    else {
                        val newList = ArrayList<Enum<*>>()
                        for(str in list) {
                            var enum: Enum<*>? = null
                            sett.values[0].javaClass.enumConstants.forEach {
                                if((it as Enum<*>).name.uppercase(Locale.getDefault()) == str.uppercase(Locale.getDefault())) enum = it
                            }

                            if(enum == null) continue
                            newList.add(enum!!)
                        }

                        (setting as Setting<List<Enum<*>>>).value = newList
                    }

                    println("ATTEMPTED TO SET LIST ${setting.name.uppercase(Locale.getDefault())} TO $value: USE \"GET\" TO CHECK")
                }

                else println("INVALID COMMAND")
            }
        }
    }
}