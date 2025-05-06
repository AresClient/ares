package org.aresclient.ares.api.setting.settings.grouped;

import java.util.Set;

public interface IGroupMember<T> {
    String getName();

    GroupMembers<T> getParent();

    void setParent(GroupMembers<T> parent);

    Set<GroupMember<T>> getChildren();
}
