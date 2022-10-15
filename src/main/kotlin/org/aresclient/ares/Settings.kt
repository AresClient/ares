package org.aresclient.ares

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.meshmc.mesh.util.render.Color
import org.aresclient.ares.Setting.Type.*
import java.io.File
import java.lang.Double.max
import java.lang.Double.min
import java.lang.Float.max
import java.lang.Float.min
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.Long.max
import java.lang.Long.min

interface Serializable {
    fun getName(): String
    fun toJSON(): JsonElement
    fun getParent(): Serializable?

    fun getFullName(): String {
        return (getParent()?.getFullName()?.let { "$it:" } ?: "") + getName()
    }
}

open class PossibleValues<T>
data class ListValues<T>(val values: List<T>): PossibleValues<ArrayList<T>>()
data class RangeValues<T: Number>(val min: T?, val max: T?): PossibleValues<T>() {
    fun noBounds(): Boolean = min == null || max == null
}

open class Setting<T>(private val name: String, val type: Type, var value: T,
val possibleValues: PossibleValues<T> = PossibleValues(), private val parent: Serializable): Serializable {
    enum class Type {
        STRING, BOOLEAN, ENUM,
        COLOR, INTEGER, DOUBLE,
        FLOAT, LONG, LIST, ARRAY,
        BIND
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
            COLOR -> {
                val obj = entry.jsonObject
                val rgba = obj["rgba"]?.jsonArray?.map { it.jsonPrimitive.floatOrNull ?: 1f }
                val rainbow = obj["rainbow"]?.jsonPrimitive?.booleanOrNull
                if(rgba == null || rainbow == null) null
                else SColor(rgba[0], rgba[1], rgba[2], rgba[3], rainbow) as T?
            }
            BIND -> entry.jsonPrimitive.intOrNull as T?
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
            ARRAY -> {
                entry.jsonArray.map {
                    Settings(it.jsonObject)
                } as T?
            }
        } ?: default
    }

    override fun toJSON(): JsonElement = when(type) {
        STRING -> JsonPrimitive(value as String)
        BOOLEAN -> JsonPrimitive(value as Boolean)
        ENUM -> JsonPrimitive((value as Enum<*>).ordinal)
        COLOR -> {
            val v = value as SColor
            JsonObject(mapOf(
                "rgba" to JsonArray(listOf(JsonPrimitive(v.red), JsonPrimitive(v.green), JsonPrimitive(v.blue), JsonPrimitive(v.alpha))),
                "rainbow" to JsonPrimitive(v.rainbow)
            ))
        }
        INTEGER, DOUBLE, FLOAT, LONG, BIND -> JsonPrimitive(value as Number)
        LIST -> JsonArray((value as List<*>).mapNotNull {
            when(it) {
                is String -> JsonPrimitive(it)
                is Enum<*> -> JsonPrimitive(it.ordinal)
                else -> null
            }
        })
        ARRAY -> JsonArray((value as List<*>).mapNotNull {
            if(it is Settings) it.toJSON() else null
        })
    }

    override fun getName(): String = name

    override fun getParent(): Serializable? = parent
}

open class Settings(private var json: JsonObject, private val jsonBuilder: JsonBuilder.() -> Unit = {},
                    private val name: String = "Home", private val parent: Serializable? = null): Serializable {
    companion object {
        fun read(file: File, jsonBuilder: JsonBuilder.() -> Unit = {}) = Settings(try {
            Json.parseToJsonElement(file.readText()).jsonObject
        } catch(e: Exception) {
            JsonObject(emptyMap())
        }, jsonBuilder)

        fun new(jsonBuilder: JsonBuilder.() -> Unit = {}) = Settings(JsonObject(mapOf()), jsonBuilder)
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

    fun string(name: String, default: String) = Setting(name, STRING, default, parent = this).read()
    fun boolean(name: String, default: Boolean) = Setting(name, BOOLEAN, default, parent = this).read()
    fun <T: Enum<*>> enum(name: String, default: T) = Setting(name, ENUM, default, parent = this).read()
    fun color(name: String, default: SColor) = Setting(name, COLOR, default, parent = this).read()
    fun color(name: String, default: Color) = color(name, SColor(default.red, default.green, default.blue, default.alpha, false))
    fun bind(name: String, default: Int) = Setting(name, BIND, default, parent = this).read()
    fun integer(name: String, default: Int, min: Int? = null, max: Int? = null) = Setting(name, INTEGER, default, RangeValues(min, max), this).read()
    fun double(name: String, default: Double, min: Double? = null, max: Double? = null) = Setting(name, DOUBLE, default, RangeValues(min, max), this).read()
    fun float(name: String, default: Float, min: Float? = null, max: Float? = null) = Setting(name, FLOAT, default, RangeValues(min, max), this).read()
    fun long(name: String, default: Long, min: Long? = null, max: Long? = null) = Setting(name, LONG, default, RangeValues(min, max), this).read()
    fun <T> list(name: String, default: ArrayList<T>, possibleValues: List<T>) = Setting(name, LIST, default, ListValues(possibleValues), this).read()
    fun array(name: String, default: ArrayList<Settings> = arrayListOf()) = Setting(name, ARRAY, default, parent = this).read()
    fun category(name: String) = Settings((json[name]?.jsonObject ?: JsonObject(emptyMap())), name = name, parent = this).also { map[name] = it }
    fun <T, R: GroupTrait> grouped(name: String, default: ArrayList<Group<T, R>>, possibles: List<T>, init: (Settings) -> R) = Grouped(this, name, default, possibles, init)

    fun clone(): Settings = Settings(toJSON(), jsonBuilder)

    @Suppress("UNCHECKED_CAST")
    private fun <T> Setting<T>.read(): Setting<T> {
        val setting = this@Settings.map.getOrPut(this.getName()) { this } as Setting<T>
        setting.value = setting.fromJSON(this@Settings.json)
        return setting
    }

    fun write(file: File) = file.writeText(writer.encodeToString(toJSON()))

    override fun getName(): String = name

    fun getMap(): MutableMap<String, Serializable> = map

    override fun toJSON() = JsonObject(map.mapValues { it.value.toJSON() })

    override fun getParent(): Serializable? = parent
}

class SColor(val red: Float, val green: Float, val blue: Float, val alpha: Float, var rainbow: Boolean) {
    companion object {
        fun rainbow(): SColor = SColor(0f, 0f, 0f, 0f, true)

        fun Color.toSColor(): SColor = SColor(red, green, blue, alpha, false)
    }

    private var color = Color(red, green, blue, alpha)

    fun getColor(offset: Long = 0L): Color = if(rainbow) rainbow(offset) else color

    fun getColors(size: Int): Array<Color> {
        val offset = 10240L / size
        return Array(size) { getColor(offset * it) }
    }

    private fun rainbow(offset: Long = 0L): Color {
        val hue = ((System.currentTimeMillis() + offset) % 10240L).toFloat() / 10240.0f
        return Color(Color.HSBtoRGB(hue, 1.0f, 1.0f))
    }
}

data class Group<T, R: GroupTrait>(val name: String, val trait: R, val values: ArrayList<T>)
open class GroupTrait(val settings: Settings)
class Grouped<T, R: GroupTrait>(parent: Settings, name: String, default: ArrayList<Group<T, R>>, private val possibles: List<T>, init: (Settings) -> R) {
    private val array = parent.array(name, ArrayList(default.map { write(it) }))
    private val groups = arrayListOf<Group<T, R>>().also {
        array.value.forEachIndexed { i, settings ->
            it.add(Group(settings.string("name", "Group $i").value, init(settings.category("trait")), settings.list("values", arrayListOf(), possibles).value))
        }
    }
    private val cache = hashMapOf<T, R?>()

    fun read(): List<Group<T, R>> = groups

    fun transform(call: (ArrayList<Group<T, R>>) -> Unit) {
        call(groups)
        write()
        cache.clear()
    }

    fun trait(key: T): R? = cache.getOrPut(key) {
        for(group in groups) {
            for(value in group.values) {
                if(value == key) return@getOrPut group.trait
            }
        }
        return@getOrPut null
    }

    private fun write() {
        array.value.clear()
        groups.forEach { group ->
            array.value.add(write(group))
        }
    }

    private fun write(group: Group<T, R>) = Settings.new().also { settings ->
        settings.string("name", group.name)
        settings.list("values", group.values, possibles)
        settings.getMap()["trait"] = group.trait.settings
    }

    fun possibles(): List<T> {
        val possible = ArrayList(possibles)
        groups.forEach { group -> group.values.forEach { possible.remove(it) } }
        return possible
    }
}
