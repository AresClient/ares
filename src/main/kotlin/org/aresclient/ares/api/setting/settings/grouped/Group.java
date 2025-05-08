package org.aresclient.ares.api.setting.settings.grouped;

import com.google.gson.JsonElement;
import org.aresclient.ares.api.setting.MapSetting;
import org.aresclient.ares.api.setting.settings.BooleanSetting;
import org.aresclient.ares.api.setting.settings.StringSetting;
import org.aresclient.ares.api.setting.settings.list.StringListSetting;

import java.util.HashSet;
import java.util.Set;

public abstract class Group<T> extends MapSetting {
    private final StringSetting title = addString("Name", "New Group");
    private final BooleanSetting enabled = addBoolean("Enabled", true);
    private final StringListSetting members = addStringList("Members");
    private final Set<T> membersCache;

    public Group() {
        this(new HashSet<>());
    }

    public Group(Set<T> defaultMembers) {
        this.membersCache = new HashSet<>(defaultMembers);
    }

    @Override
    public void read(JsonElement jsonElement) {
        super.read(jsonElement);
        membersCache.clear();
        members.stream()
                .map(it -> getGroupedParent().getPossibleMemberById(it))
                .forEach(it -> membersCache.add(it.getValue()));
    }

    @Override
    public JsonElement write() {
        members.clear();
        membersCache.stream()
                .map(it -> getGroupedParent().getPossibleMemberByValue(it))
                .forEach(it -> members.add(it.getId()));
        return super.write();
    }

    public StringSetting getTitle() {
        return title;
    }

    public BooleanSetting getEnabled() {
        return enabled;
    }

    public Set<T> getMembers() {
        return membersCache;
    }

    public GroupedSetting<T, ? extends Group<T>> getGroupedParent() {
        return (GroupedSetting<T, ? extends Group<T>>) getParent();
    }
}
