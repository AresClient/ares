package org.aresclient.ares.api.setting.settings;

import org.aresclient.ares.api.setting.Setting;
import org.aresclient.ares.api.setting.MapSetting;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.aresclient.ares.api.util.StringUtils.fromUnknownGetSettingName;

// TODO: Add a dedicated interface (a new window?) for this
public class GroupedSetting<T, V extends Setting<?>> extends MapSetting {
	public GroupedSetting(Set<T> possibleKeys, Supplier<V> generalDefault, Map<T, Supplier<V>> defaultValues) {
		for (T key: possibleKeys) {
			add(defaultValues.getOrDefault(key, generalDefault).get(), fromUnknownGetSettingName(key));
		}
	}

	public boolean containsKey(T key) {
		return getValue().containsKey(fromUnknownGetSettingName(key));
	}

	public V getValue(T key) {
		return (V) getValue().get(fromUnknownGetSettingName(key));
	}
}
