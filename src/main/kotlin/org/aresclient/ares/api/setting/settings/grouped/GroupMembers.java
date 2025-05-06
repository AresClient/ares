package org.aresclient.ares.api.setting.settings.grouped;

import java.util.HashSet;
import java.util.Set;

public class GroupMembers<T> implements IGroupMember<T> {
    public final String name;
    private GroupMembers<T> parent = null;
    private final Set<IGroupMember<T>> children = new HashSet<>();

    public GroupMembers(String name, Iterable<IGroupMember<T>> members) {
        this.name = name;

        for(IGroupMember<T> member: members) {
            member.setParent(this);
            this.children.add(member);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GroupMembers<T> getParent() {
        return parent;
    }

    @Override
    public void setParent(GroupMembers<T> parent) {
        this.parent = parent;
    }

    @Override
    public Set<GroupMember<T>> getChildren() {
        Set<GroupMember<T>> result = new HashSet<>();
        children.stream().map(IGroupMember::getChildren).forEach(result::addAll);
        return result;
    }
}
