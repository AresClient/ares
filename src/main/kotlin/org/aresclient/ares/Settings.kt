package org.aresclient.ares

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.Setting.Type.*
import java.io.File

import java.lang.Integer.*
import java.lang.Double.*
import java.lang.Float.*
import java.lang.Long.*

interface Serializable {
    fun toJSON(): JsonElement
}

open class Setting<T>(val name: String, val type: Type, var value: T, val possibleValues: PossibleValues<T> = PossibleValues()): Serializable {
    enum class Type {
        STRING, BOOLEAN, ENUM,
        COLOR, INTEGER, DOUBLE,
        FLOAT, LONG, LIST
    }

    private val default: T = value

    fun refresh(json: JsonObject) {
        this.value = fromJSON(json)
    }

    fun default() {
        this.value = default
    }

    @Suppress("UNCHECKED_CAST")
    fun fromJSON(json: JsonObject): T {
        val entry = json[name] ?: return value
        return when(type) {
            STRING -> entry.jsonPrimitive.contentOrNull as T?
            BOOLEAN -> entry.jsonPrimitive.booleanOrNull as T?
            ENUM -> entry.jsonPrimitive.intOrNull?.let { value!!::class.java.enumConstants[it] as T }
            COLOR -> entry.jsonArray.map { it.jsonPrimitive.floatOrNull ?: 1f }.let { Color(it[0], it[1], it[2], it[3]) } as T?
            INTEGER -> {
                possibleValues as RangeValues
                entry.jsonPrimitive.intOrNull?.let { n ->
                    max(possibleValues.min as Int? ?: n, min(possibleValues.max as Int? ?: n, n))
                } as T?
            }
            DOUBLE -> {
                possibleValues as RangeValues
                entry.jsonPrimitive.doubleOrNull?.let { n ->
                    max(possibleValues.min as Double? ?: n, min(possibleValues.max as Double? ?: n, n))
                } as T?
            }
            FLOAT -> {
                possibleValues as RangeValues
                entry.jsonPrimitive.floatOrNull?.let { n ->
                    max(possibleValues.min as Float? ?: n, min(possibleValues.max as Float? ?: n, n))
                } as T?
            }
            LONG -> {
                possibleValues as RangeValues
                entry.jsonPrimitive.longOrNull?.let { n ->
                    max(possibleValues.min as Long? ?: n, min(possibleValues.max as Long? ?: n, n))
                } as T?
            }
            LIST -> {
                possibleValues as ListValues<*>
                entry.jsonArray.mapNotNull {
                    it.jsonPrimitive.intOrNull?.let { index ->
                        possibleValues.values[index]
                    }
                } as T?
            }
        } ?: default
    }

    override fun toJSON(): JsonElement = when(type) {
        STRING -> JsonPrimitive(value as String)
        BOOLEAN -> JsonPrimitive(value as Boolean)
        ENUM -> JsonPrimitive((value as Enum<*>).ordinal)
        COLOR -> {
            val v = value as Color
            JsonArray(listOf(JsonPrimitive(v.red), JsonPrimitive(v.green), JsonPrimitive(v.blue), JsonPrimitive(v.alpha)))
        }
        INTEGER, DOUBLE, FLOAT, LONG -> JsonPrimitive(value as Number)
        LIST -> JsonArray((value as List<*>).mapNotNull {
            when(it) {
                is String -> JsonPrimitive(it)
                is Enum<*> -> JsonPrimitive(it.ordinal)
                else -> null
            }
        })
    }
}

open class Settings(private var json: JsonObject, jsonBuilder: JsonBuilder.() -> Unit = {}): Serializable {
    companion object {
        fun read(file: File, jsonBuilder: JsonBuilder.() -> Unit = {}) = Settings(try {
            Json.parseToJsonElement(file.readText()).jsonObject
        } catch(e: Exception) {
            JsonObject(emptyMap())
        }, jsonBuilder)
    }

    private val writer = Json(Json.Default, jsonBuilder)
    private val map = mutableMapOf<String, Serializable>()

    fun get(name: String): Serializable? {
        return map[map.keys.find { it.contentEquals(name, true) }]
    }

    fun read(file: File) {
        json =
            try {
                Json.parseToJsonElement(file.readText()).jsonObject
            } catch(e: Exception) {
                JsonObject(emptyMap())
            }

        refresh()
    }

    private fun refresh() {
        for(e in map) {
            if(e.value is Setting<*>)
                (e.value as Setting<*>).refresh(json)
            if(e.value is Settings) {
                (e.value as Settings).json = (json[e.key]?.jsonObject ?: JsonObject(emptyMap()))
                (e.value as Settings).refresh()
            }
        }
    }

    fun default() {
        for(e in map) {
            if(e.value is Setting<*>)
                (e.value as Setting<*>).default()
            if(e.value is Settings) {
                (e.value as Settings).json = (json[e.key]?.jsonObject ?: JsonObject(emptyMap()))
                (e.value as Settings).default()
            }
        }
    }

    fun string(name: String, default: String) = Setting(name, STRING, default).read()
    fun boolean(name: String, default: Boolean) = Setting(name, BOOLEAN, default).read()
    fun <T: Enum<*>> enum(name: String, default: T) = Setting(name, ENUM, default).read()
    fun color(name: String, default: Color) = Setting(name, COLOR, default).read()
    fun integer(name: String, default: Int, min: Int? = null, max: Int? = null) = Setting(name, INTEGER, default, RangeValues(min, max)).read()
    fun double(name: String, default: Double, min: Double? = null, max: Double? = null) = Setting(name, DOUBLE, default, RangeValues(min, max)).read()
    fun float(name: String, default: Float, min: Float? = null, max: Float? = null) = Setting(name, FLOAT, default, RangeValues(min, max)).read()
    fun long(name: String, default: Long, min: Long? = null, max: Long? = null) = Setting(name, LONG, default, RangeValues(min, max)).read()
    fun <T> list(name: String, default: List<T>, possibleValues: List<T>) = Setting(name, LIST, default, ListValues(possibleValues)).read()
    fun category(name: String) = Settings((json[name]?.jsonObject ?: JsonObject(emptyMap()))).also { map[name] = it }

    private fun <T> Setting<T>.read(): Setting<T> {
        this@Settings.map[this.name] = this
        this.value = this.fromJSON(this@Settings.json)
        return this
    }

    fun write(file: File) = file.writeText(writer.encodeToString(toJSON()))

    override fun toJSON() = JsonObject(map.mapValues { it.value.toJSON() })
}

open class PossibleValues<T>
data class ListValues<T>(val values: List<T>): PossibleValues<List<T>>()
data class RangeValues<T: Number>(val min: T?, val max: T?): PossibleValues<T>() {
    fun noBounds(): Boolean = min == null || max == null
}
