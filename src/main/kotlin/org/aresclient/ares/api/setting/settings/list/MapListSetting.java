package org.aresclient.ares.api.setting.settings.list;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.aresclient.ares.api.setting.MapSetting;

import java.util.ArrayList;
import java.util.List;

public class MapListSetting extends AbstractListSetting<MapSetting> {
    public MapListSetting() {
        this(new ArrayList<>());
    }

    public MapListSetting(List<MapSetting> value) {
        super(Type.MAP_LIST, value);
    }

    @Override
    protected void onAdd(MapSetting map) {
        map.setParent(this);
    }

    @Override
    protected void onRemove(MapSetting map) {
        map.setParent(null);
    }

    @Override
    public void read(JsonElement jsonElement) {
        for(MapSetting map: values) onRemove(map);
        values.clear();

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for(JsonElement childElement: jsonArray) {
            MapSetting group = new MapSetting();
            onAdd(group);
            group.read(childElement);
            values.add(group);
        }

        onChange();
    }

    @Override
    public JsonElement write() {
        JsonArray jsonArray = new JsonArray();
        for(MapSetting groups: values) jsonArray.add(groups.write());
        return jsonArray;
    }
}
