package org.aresclient.ares.api.instruments

import org.aresclient.ares.Main
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.render.Texture
import org.aresclient.ares.api.setting.Setting
import java.io.InputStream
import java.util.*

abstract class Module(category:Category, name:String, description:String, val defaults:Defaults = Defaults()): Instrument(name, description, category.settings) {
	companion object {
		internal val SETTINGS = Main.SETTINGS.addMap("Modules")
	}

	/* ---------------------------------------------------------------------- */

	enum class Category {
		PLAYER,
		OFFENCE,
		DEFENCE,
		MOVEMENT,
		RENDER,
		HUD,
		MISC;

		companion object {
			val CATEGORIES = Category.values().asList()
			fun getAll(): List<Category> = CATEGORIES
		}

		private val formattedName = name
			.lowercase()
			.replaceFirstChar { it.titlecase(Locale.getDefault()) }

		val iconFile:InputStream = javaClass.getResourceAsStream(
			"/assets/ares/textures/icons/categories" + name.lowercase() + ".png"
		)

		private lateinit var iconTexture:Texture

		fun getIcon(): Texture {
			if (iconTexture == null) iconTexture = Texture(iconFile, false)
			return iconTexture
		}

		val settings = SETTINGS.addMap(getName())
		val modules = ArrayList<Module>()

		fun getName() = formattedName

	}

	enum class ToggleOn { PRESS, RELEASE, HOLD }
	class Defaults {

		internal var enabled = false
		internal var bind = -1
		internal var toggleOn = ToggleOn.PRESS
		internal var alwaysListening = false

		fun setEnabled(value:Boolean):Defaults {
			enabled = value
			return this
		}

		fun setBind(value:Int):Defaults { bind = value; return this }
		fun setToggleOn(value:ToggleOn):Defaults { toggleOn = value; return this }
		fun setAlwaysListening(value:Boolean):Defaults { alwaysListening = value; return this }

	}

	/* ---------------------------------------------------------------------- */

	val enabled = settings
		.addBoolean("Enabled", defaults.enabled)
		.addListener { value:Boolean ->
			if (value) {
				if (!defaults.alwaysListening) registerEvents()
				onEnable()
			}
			else {
				if (!defaults.alwaysListening) unregisterEvents()
				onDisable()
			}
		} as Setting.Boolean

	val bind:Setting.Bind = settings
		.addBind("Bind", defaults.bind)
		.setCallback { state:Boolean ->
			val toggle = toggleOn.value
			if (toggle == ToggleOn.PRESS && state) toggle();
			else if (toggle == ToggleOn.RELEASE && !state) toggle();
			else if (toggle == ToggleOn.HOLD) setEnabled(state);
		}

	val toggleOn:Setting.Enum<ToggleOn> = settings.addEnum("Toggle On", defaults.toggleOn)

	/* ---------------------------------------------------------------------- */

	fun isEnabled() = enabled.value
	fun setEnabled(value:Boolean) {
		enabled.value = value
	}

	fun getBind() = bind.value
	fun setBind(value:Int) {
		bind.value = value
	}

	fun getToggleOn() = toggleOn.value
	fun setToggleOn(value:ToggleOn) {
		toggleOn.value = value
	}

	/* ---------------------------------------------------------------------- */

	fun toggle() = setEnabled(!isEnabled())
	fun isListening() = isEnabled() || defaults.alwaysListening

	/* ---------------------------------------------------------------------- */

	override fun tick() {
		if (isListening()) onTick()
	}

	fun motion() {
		if (isListening()) onMotion()
	}

	fun renderHud(delta:Float, buffers:Renderer.Buffers, matrixStack:MatrixStack) {
		if (isListening()) onRenderHud(delta, buffers, matrixStack)
	}

	fun renderWorld(delta:Float, buffers:Renderer.Buffers, matrixStack:MatrixStack) {
		if (isListening()) onRenderWorld(delta, buffers, matrixStack)
	}

	/* ---------------------------------------------------------------------- */

	protected open fun onTick() {}
	protected open fun onMotion() {}
	protected open fun onRenderHud(delta:Float, buffers:Renderer.Buffers, matrixStack:MatrixStack) {}
	protected open fun onRenderWorld(delta:Float, buffers:Renderer.Buffers, matrixStack:MatrixStack) {}

	protected open fun onEnable() {}
	protected open fun onDisable() {}

}

