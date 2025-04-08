package org.aresclient.ares.api.instruments

import org.aresclient.ares.Ares
import org.aresclient.ares.api.render.MatrixStack
import org.aresclient.ares.api.render.Renderer
import org.aresclient.ares.api.render.Texture
import org.aresclient.ares.api.setting.Setting
import java.util.*

abstract class Module(category: Category, name: String, description: String, private val defaults: Defaults = Defaults()):
	Instrument(name, description, category.settings) {
	companion object {
		internal val SETTINGS = Ares.SETTINGS.addMap("Modules")
	}

	/* ---------------------------------------------------------------------- */

	enum class Category {
		PLAYER,
		OFFENSE,
		DEFENSE,
		MOVEMENT,
		RENDER,
		HUD,
		MISC;

		companion object {
			fun getAll(): List<Category> = entries
		}

		val prettyName = name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }
		val settings = SETTINGS.addMap(prettyName)
		val modules = ArrayList<Module>()

		val icon by lazy {
			Texture(this::class.java.getResourceAsStream(
				"/assets/ares/textures/icons/categories/" + name.lowercase() + ".png"
			)!!, false)
		}
	}

	enum class ToggleOn { PRESS, RELEASE, HOLD }
	class Defaults {
		internal var enabled = false
		internal var bind = -1
		internal var toggleOn = ToggleOn.PRESS
		internal var alwaysListening = false

		fun setEnabled(value: Boolean): Defaults { enabled = value; return this }
		fun setBind(value: Int): Defaults { bind = value; return this }
		fun setToggleOn(value: ToggleOn): Defaults { toggleOn = value; return this }
		fun setAlwaysListening(value: Boolean): Defaults { alwaysListening = value; return this }
	}

	/* ---------------------------------------------------------------------- */

	private val enabled = settings
		.addBoolean("Enabled", defaults.enabled)
		.addListener { value:Boolean ->
			if(value) {
				if(!defaults.alwaysListening) registerEvents()
				onEnable()
			}
			else {
				if(!defaults.alwaysListening) unregisterEvents()
				onDisable()
			}
		} as Setting.Boolean

	private val bind: Setting.Bind = settings
		.addBind("Bind", defaults.bind)
		.setCallback { state:Boolean ->
			val toggle = toggleOn.value
			if (toggle == ToggleOn.PRESS && state) toggle();
			else if (toggle == ToggleOn.RELEASE && !state) toggle();
			else if (toggle == ToggleOn.HOLD) setEnabled(state);
		}

	private val toggleOn: Setting.Enum<ToggleOn> = settings.addEnum("Toggle On", defaults.toggleOn)

	/* ---------------------------------------------------------------------- */

	fun isEnabled(): Boolean = enabled.value
	fun setEnabled(value:Boolean) {
		enabled.value = value
	}

	fun getBind() = bind.value
	fun setBind(value: Int) {
		bind.value = value
	}

	fun getToggleOn() = toggleOn.value
	fun setToggleOn(value: ToggleOn) {
		toggleOn.value = value
	}

	/* ---------------------------------------------------------------------- */

	fun toggle() = setEnabled(!isEnabled())
	fun isListening() = isEnabled() || defaults.alwaysListening

	override fun registerEvents() = if (isListening()) super.registerEvents() else Unit
	override fun unregisterEvents() = if(!isListening()) super.unregisterEvents() else Unit

	/* ---------------------------------------------------------------------- */

	override fun tick() {
		if(isListening()) onTick()
	}

	fun motion() {
		if(isListening()) onMotion()
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
	protected open fun onRenderHud(delta: Float, buffers: Renderer.Buffers, matrixStack: MatrixStack) {}
	protected open fun onRenderWorld(delta: Float, buffers: Renderer.Buffers, matrixStack: MatrixStack) {}

	protected open fun onEnable() {}
	protected open fun onDisable() {}
}
