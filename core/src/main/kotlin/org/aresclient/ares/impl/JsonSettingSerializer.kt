package org.aresclient.ares.impl

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.aresclient.ares.api.setting.ISerializer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.setting.Setting.Type.*
import org.aresclient.ares.api.util.Color
import java.io.File
import kotlin.math.max
import kotlin.math.min

class JsonSettingSerializer(jsonBuilder: JsonBuilder.() -> Unit = {}): ISerializer<JsonElement> {
    private val writer = Json(Json.Default, jsonBuilder)

    fun read(file: File): Setting.Map<JsonElement> {
        return if(!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
            Setting.Map(this)
        } else readFromString(file.readText())
    }
    fun readFromString(text: String) = Setting.Map(this).also {
        it.read(try {
            Json.parseToJsonElement(text)
        } catch(e: Exception) {
            JsonObject(emptyMap())
        })
    }

    fun write(setting: Setting<*, JsonElement>, file: File) = file.writeText(writeToString(setting))
    fun writeToString(setting: Setting<*, JsonElement>) = writer.encodeToString(write(setting))

    override fun <R> read(setting: Setting<R, JsonElement>, data: JsonElement) {
        setting.value = (when(setting.type!!) {
            STRING -> data.jsonPrimitive.contentOrNull
            BOOLEAN -> data.jsonPrimitive.booleanOrNull
            ENUM -> data.jsonPrimitive.intOrNull?.let { setting.value!!::class.java.enumConstants[it] }
            COLOR -> {
                setting as Setting.Color
                val obj = data.jsonObject
                obj["rainbow"]?.jsonPrimitive?.booleanOrNull?.let { setting.isRainbow = it }
                obj["rgba"]?.jsonArray?.map { it.jsonPrimitive.floatOrNull ?: 1f }?.let {
                    Color(it[0], it[1], it[2], it[3])
                }
            }
            BIND -> data.jsonPrimitive.intOrNull
            INTEGER -> {
                setting as Setting.Integer
                data.jsonPrimitive.intOrNull?.let { n ->
                    max(setting.min ?: n, min(setting.max ?: n, n))
                }
            }
            DOUBLE -> {
                setting as Setting.Double
                data.jsonPrimitive.doubleOrNull?.let { n ->
                    max(setting.min ?: n, min(setting.max ?: n, n))
                }
            }
            FLOAT -> {
                setting as Setting.Float
                data.jsonPrimitive.floatOrNull?.let { n ->
                    max(setting.min ?: n, min(setting.max ?: n, n))
                }
            }
            LONG -> {
                setting as Setting.Long
                data.jsonPrimitive.longOrNull?.let { n ->
                    max(setting.min ?: n, min(setting.max ?: n, n))
                }
            }
            GROUPED -> TODO()
            LIST -> {
                setting as Setting.List<*, JsonElement>
                data.jsonArray.mapIndexed { i, elem ->
                    var value = readListValue(setting.elementType, elem) ?: setting.defaultValue
                    if(setting.type == MAP) value = Setting.Map(this, "$i", value as Map<String, JsonElement>)
                    value
                }
            }
            MAP -> data.jsonObject.toMutableMap()
        } ?: setting.defaultValue) as R
    }

    override fun <R> write(setting: Setting<R, JsonElement>): JsonElement {
        return when(setting.type!!) {
            STRING -> JsonPrimitive(setting.value as String)
            BOOLEAN -> JsonPrimitive(setting.value as Boolean)
            ENUM -> JsonPrimitive((setting.value as Enum<*>).ordinal)
            COLOR -> {
                setting as Setting.Color
                JsonObject(mapOf(
                    "rgba" to JsonArray(listOf(JsonPrimitive(setting.value.red), JsonPrimitive(setting.value.green),
                        JsonPrimitive(setting.value.blue), JsonPrimitive(setting.value.alpha))),
                    "rainbow" to JsonPrimitive(setting.isRainbow)
                ))
            }
            INTEGER, DOUBLE, FLOAT, LONG, BIND -> JsonPrimitive(setting.value as Number)
            GROUPED -> TODO()
            LIST -> {
                setting as Setting.List<*, JsonElement>
                JsonArray(setting.value.map { writeListValue(setting.elementType, it) })
            }
            MAP -> {
                setting as Setting.Map<JsonElement>
                JsonObject(setting.value as Map<String, JsonElement>)
            }
        }
    }

    private fun readListValue(type: Setting.Type, jsonElement: JsonElement): Any? {
        return when(type) {
            STRING -> jsonElement.jsonPrimitive.contentOrNull
            BOOLEAN -> jsonElement.jsonPrimitive.booleanOrNull
            COLOR -> {
                val red = jsonElement.jsonObject["red"]?.jsonPrimitive?.floatOrNull
                val green = jsonElement.jsonObject["green"]?.jsonPrimitive?.floatOrNull
                val blue = jsonElement.jsonObject["blue"]?.jsonPrimitive?.floatOrNull
                val alpha = jsonElement.jsonObject["alpha"]?.jsonPrimitive?.floatOrNull
                if(red == null || green == null || blue == null || alpha == null) null
                else Color(red, green, blue, alpha)
            }
            INTEGER -> jsonElement.jsonPrimitive.intOrNull
            DOUBLE -> jsonElement.jsonPrimitive.doubleOrNull
            FLOAT -> jsonElement.jsonPrimitive.floatOrNull
            LONG -> jsonElement.jsonPrimitive.longOrNull
            MAP -> jsonElement.jsonObject.toMap()
            else -> throw RuntimeException("Unsupported type in list!")
        }
    }

    private fun writeListValue(type: Setting.Type, value: Any): JsonElement {
        return when(type) {
            STRING -> JsonPrimitive(value as String)
            BOOLEAN -> JsonPrimitive(value as Boolean)
            COLOR -> {
                value as Color
                JsonObject(mapOf(
                    "red" to JsonPrimitive(value.red),
                    "green" to JsonPrimitive(value.green),
                    "blue" to JsonPrimitive(value.blue),
                    "alpha" to JsonPrimitive(value.alpha)
                ))
            }
            INTEGER -> JsonPrimitive(value as Int)
            DOUBLE -> JsonPrimitive(value as Double)
            FLOAT -> JsonPrimitive(value as Float)
            LONG -> JsonPrimitive(value as Long)
            MAP -> {
                JsonObject(value as Map<String, JsonElement>)
            }
            else -> throw RuntimeException("Unsupported type in list!")
        }
    }
}