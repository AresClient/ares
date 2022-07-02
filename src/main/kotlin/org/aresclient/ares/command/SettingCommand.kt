package org.aresclient.ares.command

import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.global.Global
import org.aresclient.ares.Setting
import org.aresclient.ares.Settings
import org.aresclient.ares.module.Module
import java.util.*

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
        if(command[1].startsWith("s")) set(setting, command[command.size - 1])
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
    private fun set(setting: Setting<*>, value: String) {
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
                    (setting as Setting<Int>).value = int
                    println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO $int")
                }
            }
            Setting.Type.DOUBLE -> {
                val double = value.toDoubleOrNull()
                if(double != null) (setting as Setting<Double>).value = double
                println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO $double")
            }
            Setting.Type.FLOAT -> {
                val float = value.toFloatOrNull()
                if(float != null) (setting as Setting<Float>).value = float
                println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO $float")
            }
            Setting.Type.LONG -> {
                val long = value.toLongOrNull()
                if(long != null) (setting as Setting<Long>).value = long
                println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO $long")
            }
            Setting.Type.LIST -> {
                (setting as Setting<List<String>>).value = value.split(",")
                println("SUCCESSFULLY SET ${setting.name.uppercase(Locale.getDefault())} TO $value")
            }
        }
    }
}