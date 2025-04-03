package org.aresclient.ares.api.util

import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt

class Color(val red:Float, val green:Float, val blue:Float, val alpha:Float) {

	constructor(rgb:Int): this(
		(rgb shr 16) / 255F,
		(rgb shr 8 and 255) / 255F,
		(rgb and 255) / 255F,
		1F
	)

	fun deriveRed(value:Float) = Color(value, green, blue, alpha)
	fun deriveGreen(value:Float) = Color(red, value, blue, alpha)
	fun deriveBlue(value:Float) = Color(red, green, value, alpha)
	fun deriveAlpha(value:Float) = Color(red, green, blue, value)

	fun getRGB():Int =
		(((alpha * 255 + 0.5).toInt() and 0xFF) shl 24) or
		(((red * 255 + 0.5).toInt() and 0xFF) shl 16) or
		(((green * 255 + 0.5).toInt() and 0xFF) shl 8) or
		(((blue * 255 + 0.5).toInt() and 0xFF))

	fun getColorBetween(color:Color):Color {
		return getColorBetween(Color(red, green, blue, alpha), color)
	}

	override fun toString():String = floatArrayOf(red, green, blue, alpha).contentToString()

	override fun equals(o: Any?):Boolean {
		if (this === o) return true
		if (o == null || javaClass != o.javaClass) return false
		val color = o as Color
		return compareValues(red, color.red) == 0 && compareValues(green, color.green) == 0 && compareValues(
			blue, color.blue
		) == 0 && compareValues(alpha, color.alpha) == 0
	}

	override fun hashCode(): Int = Objects.hash(red, green, blue, alpha)

	companion object {
		val BLACK:Color = Color(0f, 0f, 0f, 1f)
		val GRAY:Color = Color(0.5f, 0.5f, 0.5f, 1f)
		val WHITE:Color = Color(1f, 1f, 1f, 1f)
		val RED:Color = Color(1f, 0f, 0f, 1f)
		val GREEN:Color = Color(0f, 1f, 0f, 1f)
		val BLUE:Color = Color(0f, 0f, 1f, 1f)
		val COLORLESS:Color = Color(0f, 0f, 0f, 0f)

		fun fromDistance(distance:Float):Color {
			val fraction = if (distance > 50) 1.0 else distance / 50.0
			return if (fraction > 0.5)
				Color(1F - (fraction.toFloat() - 0.5F) * 2F, 1F, 0F, 1F)
			else
				Color(1F, fraction.toFloat() * 2F, 0F, 1F)
		}

		fun convert(red:Float, green:Float, blue:Float, alpha:Float):Int {
			require(
				!(red < 0 || red > 1
					|| green < 0 || green > 1
					|| blue < 0 || blue > 1
					|| alpha < 0 || alpha > 1
					)
			) { "Bad RGB values" }
			val redval = (255 * red).roundToInt()
			val greenval = (255 * green).roundToInt()
			val blueval = (255 * blue).roundToInt()
			val alphaval = (255 * alpha).roundToInt()
			return (alphaval shl 24) or (redval shl 16) or (greenval shl 8) or blueval
		}

		fun HSBtoRGB(hue:Float, saturation:Float, brightness:Float):Int {
			if (saturation == 0F) return convert(brightness, brightness, brightness, 0F)
			require(!(saturation < 0 || saturation > 1 || brightness < 0 || brightness > 1))
			val h = hue - floor(hue)
			val i = (6 * hue).toInt()
			val f = 6F * h - i
			val p = brightness * (1 - saturation)
			val q = brightness * (1 - saturation * f)
			val t = brightness * (1 - saturation * (1 - f))
			return when (i) {
				0    -> convert(brightness, t, p, 0f)
				1    -> convert(q, brightness, p, 0f)
				2    -> convert(p, brightness, t, 0f)
				3    -> convert(p, q, brightness, 0f)
				4    -> convert(t, p, brightness, 0f)
				5    -> convert(brightness, p, q, 0f)
				else -> throw InternalError("impossible")
			}
		}

		@JvmStatic fun rainbow():Color {
			val hue = (System.currentTimeMillis() % (320 * 32)) / (320f * 32)
			return Color(HSBtoRGB(hue, 1f, 1f))
		}

		@JvmStatic fun rainbow(speed:Int, offset:Float, saturation:Float, brightness:Float):Color {
			val hue = ((System.currentTimeMillis() % ((speed * 10) * speed)) / ((speed * 10f) * speed)) - offset
			var hue2 = hue
			if (hue < 0) hue2 = 1f + hue
			return Color(HSBtoRGB(hue2, saturation, brightness))
		}

		fun getColorBetween(color1:Color, color2:Color):Color {
			val r = (color1.red + color2.red) / 2
			val g = (color1.green + color2.green) / 2
			val b = (color1.blue + color2.blue) / 2
			val a = (color1.alpha + color2.alpha) / 2
			return Color(r, g, b, a)
		}
	}

}

