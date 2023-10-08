package org.aresclient.ares.impl

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.aresclient.ares.api.setting.ISerializer
import org.aresclient.ares.api.setting.Setting
import org.aresclient.ares.api.util.Color
import java.io.File

class JsonSettingSerializer(jsonBuilder: JsonBuilder.() -> Unit = {}): ISerializer<JsonElement> {
    private val writer = Json(Json, jsonBuilder)

    fun read(file: File): Setting.Map<JsonElement> {
        return if(!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
            Setting.Map(this)
        } else readFromString(file.readText())
    }

    fun readFromString(text: String) = Setting.Map(this, try {
        Json.parseToJsonElement(text).jsonObject
    } catch(e: Exception) {
        JsonObject(emptyMap())
    }.toMutableMap())

    fun write(setting: Setting<*>, file: File) = file.writeText(writeToString(setting))
    fun writeToString(setting: Setting<*>) = writer.encodeToString(write(setting))

    override fun read(readInfo: Setting.ReadInfo<*>?, data: JsonElement?): Setting<*> {
        return when(readInfo?.type) {
            Setting.Type.STRING -> Setting.String(data?.jsonPrimitive?.contentOrNull ?: readInfo.defaultValue as String)
            Setting.Type.BOOLEAN -> Setting.Boolean(data?.jsonPrimitive?.booleanOrNull ?: readInfo.defaultValue as Boolean)
            Setting.Type.ENUM -> Setting.Enum((data?.jsonPrimitive?.intOrNull?.let { readInfo.defaultValue!!::class.java.enumConstants[it] } ?: readInfo.defaultValue) as Enum<*>)
            Setting.Type.COLOR -> data?.jsonObject?.let {  Setting.Color(Color(
                it["red"]?.jsonPrimitive?.floatOrNull ?: 1f,
                it["green"]?.jsonPrimitive?.floatOrNull ?: 1f,
                it["blue"]?.jsonPrimitive?.floatOrNull ?: 1f,
                it["alpha"]?.jsonPrimitive?.floatOrNull ?: 1f),
                it["rainbow"]?.jsonPrimitive?.booleanOrNull ?: false
                )} ?: Setting.Color(readInfo.defaultValue as Color, readInfo.isRainbow)
            Setting.Type.INTEGER -> Setting.Integer(data?.jsonPrimitive?.intOrNull ?: readInfo.defaultValue as Int)
            Setting.Type.DOUBLE -> Setting.Double(data?.jsonPrimitive?.doubleOrNull ?: readInfo.defaultValue as Double)
            Setting.Type.FLOAT -> Setting.Float(data?.jsonPrimitive?.floatOrNull ?: readInfo.defaultValue as Float)
            Setting.Type.LONG -> Setting.Long(data?.jsonPrimitive?.longOrNull ?: readInfo.defaultValue as Long)
            Setting.Type.BIND -> Setting.Bind(data?.jsonPrimitive?.intOrNull ?: readInfo.defaultValue as Int)
            Setting.Type.GROUPED -> TODO()
            Setting.Type.LIST -> Setting.List(data?.jsonArray?.map { read(Setting.ReadInfo(readInfo.elementType, null), it) }?.toTypedArray() ?: arrayOf())
            Setting.Type.MAP -> Setting.Map(this, data?.jsonObject ?: mutableMapOf())
            null -> throw NullPointerException()
        }
    }

    override fun write(setting: Setting<*>?): JsonElement {
        return when(setting?.type) {
            Setting.Type.STRING -> JsonPrimitive(setting.value as String)
            Setting.Type.BOOLEAN -> JsonPrimitive(setting.value as Boolean)
            Setting.Type.ENUM -> JsonPrimitive((setting.value as Enum<*>).ordinal)
            Setting.Type.COLOR -> with(setting.value as Color) { JsonObject(mapOf(
                "red" to JsonPrimitive(red),
                "green" to JsonPrimitive(green),
                "blue" to JsonPrimitive(blue),
                "alpha" to JsonPrimitive(alpha),
                "rainbow" to JsonPrimitive((setting as Setting.Color).isRainbow)
            ))}
            Setting.Type.INTEGER -> JsonPrimitive(setting.value as Int)
            Setting.Type.DOUBLE -> JsonPrimitive(setting.value as Double)
            Setting.Type.FLOAT -> JsonPrimitive(setting.value as Float)
            Setting.Type.LONG -> JsonPrimitive(setting.value as Long)
            Setting.Type.BIND -> JsonPrimitive(setting.value as Int)
            Setting.Type.GROUPED -> TODO()
            Setting.Type.LIST -> JsonArray((setting.value as Array<Setting<*>>).map { write(it) })
            Setting.Type.MAP -> JsonObject((setting.value as Map<String, Setting<*>>).mapValues { write(it.value) })
            null -> throw NullPointerException()
        }
    }
}