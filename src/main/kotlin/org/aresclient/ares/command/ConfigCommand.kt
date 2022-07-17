package org.aresclient.ares.command

import org.aresclient.ares.Ares
import java.io.File
import java.util.*

object ConfigCommand: Command("allows the creation and loading of different configurations", "config", "conf", "configuration") {
    override fun execute(command: ArrayList<String>) {
        if(command.size > 2) command[2] = command[2].replace('_', ' ')
        when(command[1].lowercase(Locale.getDefault())) {
            "new" -> new(command)
            "delete" -> delete(command)
            "load" -> load(command)
            "copyto" -> copyTo(command)
            "copyfrom" -> copyFrom(command)
            "rename" -> rename(command)
            "save" -> save()
            "list" -> list()
            "current" -> current()
            else -> {
                println("INVALID CONFIG COMMAND")
                return
            }
        }
    }

    private fun new(command: ArrayList<String>) {
        if(!File("ares/configs").exists()) File("ares/configs").mkdir()

        if(containsIllegalCharacters(command[2])) {
            println("FILENAME CONTAINS ILLEGAL CHARACTERS")
            return
        }

        // save current settings file
        Ares.SETTINGS.write(Ares.SETTINGS_FILE)

        Ares.SETTINGS_FILE = File("ares/configs/${command[2]}.json")
        Ares.SETTINGS.default()
    }

    private fun delete(command: ArrayList<String>) {
        val file = File("ares/configs/${command[2]}.json")
        if(!file.exists()) {
            println("CONFIG ${command[2]} NOT FOUND")
            return
        }
        file.delete()
    }

    private fun load(command: ArrayList<String>) {
        val file = File("ares/configs/${command[2]}.json")
        if(!file.exists()) {
            println("CONFIG ${command[2]} NOT FOUND")
            return
        }
        Ares.SETTINGS.write(Ares.SETTINGS_FILE)

        Ares.SETTINGS_FILE = file
        Ares.SETTINGS.read(Ares.SETTINGS_FILE)
    }

    private fun copyTo(command: ArrayList<String>) {
        if(containsIllegalCharacters(command[2])) {
            println("FILENAME CONTAINS ILLEGAL CHARACTERS")
            return
        }

        Ares.SETTINGS.write(Ares.SETTINGS_FILE)
        Ares.SETTINGS_FILE.copyTo(File("ares/configs/${command[2]}.json"), true)
    }

    private fun copyFrom(command: ArrayList<String>) {
        val file = File("ares/configs/${command[2]}.json")
        if(!file.exists()) {
            println("CONFIG ${command[2]} NOT FOUND")
            return
        }

        file.copyTo(Ares.SETTINGS_FILE, true)
        Ares.SETTINGS.read(Ares.SETTINGS_FILE)
    }

    private fun rename(command: ArrayList<String>) {
        val file = File("ares/configs/${command[2]}.json")
        if(command.size == 4) {
            if(containsIllegalCharacters(command[2])) {
                println("FILENAME CONTAINS ILLEGAL CHARACTERS")
                return
            }

            if(!file.exists()) {
                println("CONFIG ${command[2]} NOT FOUND")
                return
            }

            command[3] = command[3].replace('_', ' ')

            if(!file.renameTo(File("ares/configs/${command[3]}.json"))) {
                println("FILE RENAMING FAILED!")
            }
        }
        else {
            Ares.SETTINGS.write(Ares.SETTINGS_FILE)
            Ares.SETTINGS_FILE.renameTo(file)
            Ares.SETTINGS_FILE = file
            Ares.SETTINGS.read(Ares.SETTINGS_FILE)
        }
    }

    private fun save() {
        Ares.SETTINGS.write(Ares.SETTINGS_FILE)
    }

    private fun list() {
        val dir = File("ares/configs")
        println("CONFIGS:")
        if(dir.isDirectory) {
            val configList = dir.list()!!.toMutableList()
            for(i in 0 until configList.size) {
                if(!configList[i].contains(".json")) continue
                configList[i] = configList[i].removeSuffix(".json")
                println(configList[i])
            }

        }
    }

    private fun current() {
        println("CURRENT CONFIG: ${Ares.SETTINGS_FILE.path.substring(13).removeSuffix(".json")}")
    }

    private fun containsIllegalCharacters(name: String): Boolean =
        '#' in name  || '$' in name  || '\\' in name || '@' in name  ||
        '%' in name  || '!' in name  || '<' in name  || '+' in name  ||
        '&' in name  || '\'' in name || '>' in name  || '`' in name  ||
        '{' in name  || '\"' in name || '*' in name  || '|' in name  ||
        '}' in name  || ':' in name  || '/' in name  || '=' in name
}
