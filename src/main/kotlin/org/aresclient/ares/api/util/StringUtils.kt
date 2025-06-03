package org.aresclient.ares.api.util

import net.minecraft.entity.EntityType

object StringUtils {
	@JvmStatic fun String?.formatToPretty(): String = this?.split('_')?.joinToString(separator = " ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } } ?: "<null>"

	// Used for GroupedSetting, maybe we can replace this with enum interface/duck interface and dedicated methods?
	@JvmStatic fun <T> T.fromUnknownGetSettingName(): String = when (this) {
		is String -> this
		is Enum<*> -> this.name.formatToPretty()
		is EntityType<*> -> this.untranslatedName.formatToPretty()
		else -> this.toString()
	}
}
