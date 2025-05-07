package org.aresclient.ares.api.setting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import org.aresclient.ares.api.setting.settings.grouped.GroupedSetting;
import org.aresclient.ares.api.setting.settings.list.AbstractListSetting;
import org.aresclient.ares.api.setting.settings.list.MapListSetting;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Setting<T> {
	public enum Type {
		STRING, BOOLEAN, ENUM,
		COLOR, INTEGER, DOUBLE,
		FLOAT, LONG, BIND,
		MAP, MAP_LIST, STRING_LIST,
		ENUM_LIST, GROUPED
	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private Setting<?> parent = null;
	private java.lang.String name = null;
	private java.lang.String[] description = null;
	private Supplier<java.lang.Boolean> visible = () -> true;
	private final java.util.List<Consumer<T>> listeners = new ArrayList<>();

	private final Type type;
	private final T defaultValue;
	private T value;

	protected Setting(Type type, T value) {
		this.type = type;
		this.defaultValue = value;
		this.value = value;
	}

	public abstract void read(JsonElement jsonElement);

	public void read(String json) throws JsonSyntaxException {
		read(GSON.fromJson(json, JsonElement.class));
	}

	public void read(File file) throws IOException {
		FileReader fileReader = new FileReader(file);
		read(GSON.fromJson(fileReader, JsonElement.class));
		fileReader.close();
	}

	public abstract JsonElement write();

	public void write(File file) throws IOException {
		FileWriter fileWriter = new FileWriter(file);
		GSON.toJson(write(), fileWriter);
		fileWriter.close();
	}

	public String writeToString() {
		return GSON.toJson(write());
	}

	protected void onChange() {
		for(Consumer<T> listener: listeners) listener.accept(value);
	}

	public Setting<?> getParent() {
		return parent;
	}

	public void setParent(Setting<?> parent) {
		this.parent = parent;
	}

	public String getName() {
		if(getParent() instanceof MapListSetting || getParent() instanceof GroupedSetting<?,?>)
			return java.lang.String.valueOf(((AbstractListSetting<?>) getParent()).indexOf(this));
		return name;
	}

	public Setting<T> setName(String name) {
		this.name = name;
		return this;
	}

	public String getPath() {
		String prefix = null;
		if(getParent() != null) prefix = getParent().getPath();
		return prefix == null ? getName() : prefix + ":" + getName();
	}

	public String[] getDescription() {
		return description;
	}

	public Setting<T> setDescription(String... description) {
		this.description = description;
		return this;
	}

	public java.lang.Boolean isVisible() {
		return visible.get();
	}

	public Setting<T> setVisibility(Supplier<java.lang.Boolean> hidden) {
		this.visible = hidden;
		return this;
	}

	public Setting<T> addListener(Consumer<T> listener) {
		listeners.add(listener);
		return this;
	}

	public Setting<T> removeListener(Consumer<T> listener) {
		listeners.remove(listener);
		return this;
	}

	public java.util.List<Consumer<T>> getListeners() {
		return listeners;
	}

	public Type getType() {
		return type;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		T prev = this.value;
		this.value = value;
		if(prev != value) onChange();
	}

	public void setDefault() {
		this.value = defaultValue;
	}

	@Override
	public java.lang.String toString() {
		return "Setting(type: " + type.name() + ", value: " + value.toString() + ", name: " + (name == null ? "<null>" : name) + ")";
	}
}
