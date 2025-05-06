package org.aresclient.ares.api.setting.settings.grouped;

import java.util.Set;

public class GroupMember<T> implements IGroupMember<T> {
    private final String id;
    private final String name;
    private final T value;
    private GroupMembers<T> parent = null;

    public GroupMember(String id, String name, T value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    @Override
    public GroupMembers<T> getParent() {
        return parent;
    }

    public void setParent(GroupMembers<T> parent) {
        this.parent = parent;
    }

    @Override
    public Set<GroupMember<T>> getChildren() {
        return Set.of(this);
    }
}
