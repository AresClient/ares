package org.aresclient.ares

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.Setting.Type.*
import java.io.File

interface Serializable {
    fun toJSON(): JsonElement
}

open class Setting<T>(val name: String, val type: Type, var value: T, val possibleValues: PossibleValues<T> = PossibleValues()): Serializable {
    private val default: T = value
    enum class Type {
        STRING, BOOLEAN, ENUM,
        COLOR, INTEGER, DOUBLE,
        FLOAT, LONG, LIST
    }

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
            STRING -> entry.jsonPrimitive.content as T
            BOOLEAN -> entry.jsonPrimitive.boolean as T
            ENUM -> value!!::class.java.enumConstants[entry.jsonPrimitive.int] as T
            COLOR -> toColor(toFloatArray(entry.jsonArray)) as T
            INTEGER -> {
                possibleValues as RangeValues
                if(possibleValues.noBounds()) entry.jsonPrimitive.int as T
                else if(possibleValues.min as Int > entry.jsonPrimitive.int) possibleValues.min
                else if(entry.jsonPrimitive.int > possibleValues.max as Int) possibleValues.max
                else entry.jsonPrimitive.int as T
            }
            DOUBLE -> {
                possibleValues as RangeValues
                if(possibleValues.noBounds()) entry.jsonPrimitive.double as T
                else if(possibleValues.min as Double > entry.jsonPrimitive.double) possibleValues.min
                else if(entry.jsonPrimitive.double > possibleValues.max as Double) possibleValues.max
                else entry.jsonPrimitive.double as T
            }
            FLOAT -> {
                possibleValues as RangeValues
                if(possibleValues.noBounds()) entry.jsonPrimitive.float as T
                else if(possibleValues.min as Float > entry.jsonPrimitive.float) possibleValues.min
                else if(entry.jsonPrimitive.float > possibleValues.max as Float) possibleValues.max
                else entry.jsonPrimitive.float as T
            }
            LONG -> {
                possibleValues as RangeValues
                if(possibleValues.noBounds()) entry.jsonPrimitive.long as T
                else if(possibleValues.min as Long > entry.jsonPrimitive.long) possibleValues.min
                else if(entry.jsonPrimitive.long > possibleValues.max as Long) possibleValues.max
                else entry.jsonPrimitive.long as T
            }
            LIST ->
                if((possibleValues as ListValues<*>).values[0]!!::class.java.isEnum) entry.jsonArray.map {
                    val v = possibleValues.values[0]!!::class.java.enumConstants[it.jsonPrimitive.int]
                    if(possibleValues.values.contains(v)) v
                    else null
                } as T
                else entry.jsonArray.map {
                    val v = it.jsonPrimitive.toString()
                    if(possibleValues.values.contains(v)) v
                    else null
                } as T
        }
    }

    private fun toFloatArray(jsonArray: JsonArray): FloatArray {
        val f: FloatArray = FloatArray(4)
        for(i in 0..3) {
            try {
                f[i] = jsonArray[i].jsonPrimitive.float
            } catch(e: Exception) {
                println("Setting $name not successfully read, returning WHITE")
                return floatArrayOf(1.0F, 1.0F, 1.0F, 1.0F)
            }
        }
        return f
    }

    private fun toColor(values: FloatArray): Color {
        return Color(values[0], values[1], values[2], values[3])
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
        for(key in map.keys)
            if(key.contentEquals(name, true))
                return map[key]

        return null
    }

    fun refreshFromFile(file: File) {
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