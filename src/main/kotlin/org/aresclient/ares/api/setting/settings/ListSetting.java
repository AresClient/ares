package org.aresclient.ares.api.setting.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.aresclient.ares.api.setting.Setting;
import org.aresclient.ares.api.setting.MapSetting;

import java.util.ArrayList;

public class ListSetting extends Setting<ArrayList<MapSetting>> {
    public ListSetting() {
        super(Type.LIST, new ArrayList<>());
    }

    public void add(MapSetting group) {
        group.setParent(this);
        getValue().add(group);
    }

    public void remove(int index) {
        remove(getValue().get(index));
    }

    public void remove(MapSetting group) {
        group.setParent(null);
        getValue().remove(group);
    }

    public int indexOf(MapSetting group) {
        return getValue().indexOf(group);
    }

    @Override
    public void setDefault() {
        getValue().forEach(group -> group.setParent(null));
        getValue().clear();
    }

    @Override
    public void read(JsonElement jsonElement) {
        setDefault();

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for(JsonElement childElement: jsonArray) {
            MapSetting group = new MapSetting();
            group.read(childElement);
            add(group);
        }
    }

    @Override
    public JsonElement write() {
        JsonArray jsonArray = new JsonArray();
        for(MapSetting groups: getValue()) jsonArray.add(groups.write());
        return jsonArray;
    }
}
