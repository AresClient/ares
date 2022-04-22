package org.aresclient.ares

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.Setting.Type.*
import java.io.File

interface Serializable {
    fun toJSON(): JsonElement
}

open class Setting<T>(val name: String, private val type: Type, public var value: T): Serializable {
    enum class Type {
        STRING, BOOLEAN, ENUM,
        COLOR, INTEGER, DOUBLE,
        FLOAT, LONG, LIST
    }

    @Suppress("UNCHECKED_CAST")
    fun fromJSON(json: JsonObject): T {
        val entry = json[name] ?: return value
        return when(type) {
            STRING -> entry.jsonPrimitive.content as T
            BOOLEAN -> entry.jsonPrimitive.boolean as T
            ENUM -> value!!::class.java.enumConstants[entry.jsonPrimitive.int] as T
            COLOR -> Color(entry.jsonPrimitive.int) as T
            INTEGER -> entry.jsonPrimitive.int as T
            DOUBLE -> entry.jsonPrimitive.double as T
            FLOAT -> entry.jsonPrimitive.float as T
            LONG -> entry.jsonPrimitive.long as T
            LIST -> entry.jsonArray.map { it.jsonPrimitive.toString() } as T
        }
    }

    override fun toJSON(): JsonElement = when(type) {
        STRING -> JsonPrimitive(value as String)
        BOOLEAN -> JsonPrimitive(value as Boolean)
        ENUM -> JsonPrimitive((value as Enum<*>).ordinal)
        COLOR -> JsonPrimitive((value as Color).rgb)
        INTEGER, DOUBLE, FLOAT, LONG -> JsonPrimitive(value as Number)
        LIST -> JsonArray((value as List<*>).mapNotNull { if(it is String) JsonPrimitive(it) else null })
    }
}

open class Settings(private val json: JsonObject, jsonBuilder: JsonBuilder.() -> Unit = {}): Serializable {
    companion object {
        fun read(file: File, jsonBuilder: JsonBuilder.() -> Unit = {}) = Settings(try {
            Json.parseToJsonElement(file.readText()).jsonObject
        } catch(e: Exception) {
            JsonObject(emptyMap())
        }, jsonBuilder)
    }

    private val writer = Json(Json.Default, jsonBuilder)
    private val map = mutableMapOf<String, Serializable>()

    fun string(name: String, default: String) = Setting(name, STRING, default).read()
    fun boolean(name: String, default: Boolean) = Setting(name, BOOLEAN, default).read()
    fun <T: Enum<*>> enum(name: String, default: T) = Setting(name, ENUM, default).read()
    fun color(name: String, default: Color) = Setting(name, COLOR, default).read()
    fun integer(name: String, default: Int) = Setting(name, INTEGER, default).read()
    fun double(name: String, default: Double) = Setting(name, DOUBLE, default).read()
    fun float(name: String, default: Float) = Setting(name, FLOAT, default).read()
    fun long(name: String, default: Long) = Setting(name, LONG, default).read()
    fun list(name: String, default: List<String>) = Setting(name, LIST, default).read()
    fun category(name: String) = Settings((json[name]?.jsonObject ?: JsonObject(emptyMap()))).also { map[name] = it }

    private fun <T> Setting<T>.read(): Setting<T> {
        this@Settings.map[this.name] = this
        this.value = this.fromJSON(this@Settings.json)
        return this
    }

    fun write(file: File) = file.writeText(writer.encodeToString(toJSON()))

    override fun toJSON() = JsonObject(map.mapValues { it.value.toJSON() })
}
