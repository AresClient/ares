package org.aresclient.ares.api.instruments

import org.aresclient.ares.Ares
import org.aresclient.ares.api.events.ToggleEvent
import org.aresclient.ares.api.render.Texture
import org.aresclient.ares.api.setting.MapSetting
import org.aresclient.ares.api.setting.settings.BindSetting
import org.aresclient.ares.api.setting.settings.BooleanSetting
import org.aresclient.ares.api.setting.settings.EnumSetting
import java.util.*

open class Module(val category: Category, val name: String, val description: String, private val defaults: Defaults = Defaults()):
	Instrument(category.settings.addMap(name).setDescription(description) as MapSetting) {
	companion object {
		internal val SETTINGS = Ares.getSettings().addMap("Modules")
	}

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

		internal var externalModuleList = true
		internal var externalToggleList = false

		fun setEnabled(value: Boolean): Defaults { enabled = value; return this }
		fun setBind(value: Int): Defaults { bind = value; return this }
		fun setToggleOn(value: ToggleOn): Defaults { toggleOn = value; return this }
		fun setAlwaysListening(value: Boolean): Defaults { alwaysListening = value; return this }

		fun setExternalModuleList(value: Boolean): Defaults { externalModuleList = value; return this }
		fun setExternalToggleList(value: Boolean): Defaults { externalToggleList = value; return this }
	}

	class ExternalCommons(private val parent: Module, defaults: Defaults): MapSetting() {
		private val externals = parent.settings.addMap("External Commons")

		private val moduleList = externals.addBoolean("Module List", defaults.externalModuleList);
		private val toggleList = externals.addBoolean("Toggle List", defaults.externalToggleList);

		val showOnModuleList: Boolean get() = parent.isEnabled() && moduleList.value
		val showOnToggleList: Boolean get() = toggleList.value
	}

	/* ---------------------------------------------------------------------- */

	private val enabled = settings
		.addBoolean("Enabled", defaults.enabled)
		.addListener { value: Boolean ->
			if(value) {
				if(!defaults.alwaysListening) registerEvents()
				onEnable()
				EVENTS.post(ToggleEvent(this, true))
			}
			else {
				if(!defaults.alwaysListening) unregisterEvents()
				onDisable()
				EVENTS.post(ToggleEvent(this, false))
			}
		} as BooleanSetting

	private val bind: BindSetting = settings
		.addBind("Bind", defaults.bind)
		.setCallback { state: Boolean, repeat: Boolean ->
			val toggle = toggleOn.value
			if (toggle == ToggleOn.PRESS && state && !repeat) toggle();
			else if (toggle == ToggleOn.RELEASE && !state) toggle();
			else if (toggle == ToggleOn.HOLD && !repeat) setEnabled(state);
		}

	private val toggleOn: EnumSetting<ToggleOn> = settings.addEnum("Toggle On", defaults.toggleOn)

	internal val externalCommons = ExternalCommons(this, defaults)

	/* ---------------------------------------------------------------------- */

	fun isEnabled(): Boolean = enabled.value
	fun setEnabled(value: Boolean) {
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

	fun toggle() = setEnabled(!isEnabled())

	override fun isListening() = isEnabled() || defaults.alwaysListening

	/* ---------------------------------------------------------------------- */

	open fun onEnable() {}
	open fun onDisable() {}

	open fun getInfo(): String? = null
}
