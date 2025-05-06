package org.aresclient.ares.api.setting.settings.grouped;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.aresclient.ares.api.setting.settings.list.AbstractListSetting;

import java.util.*;
import java.util.function.Supplier;

public class GroupedSetting<T, V extends Group<T>> extends AbstractListSetting<V> {
    private final Set<IGroupMember<T>> possibleMembers;
    private final Supplier<V> groupSupplier;

    private final Map<String, GroupMember<T>> possibleMembersById = new HashMap<>();
    private final Map<T, GroupMember<T>> possibleMembersByValue = new HashMap<>();

    public GroupedSetting(ArrayList<V> value, Set<IGroupMember<T>> possibleMembers, Supplier<V> groupSupplier) {
        super(Type.GROUPED, new ArrayList<>(value));
        this.possibleMembers = possibleMembers;
        this.groupSupplier = groupSupplier;

        for(IGroupMember<T> root: possibleMembers) {
            for(GroupMember<T> member: root.getChildren()) {
                possibleMembersById.put(member.getId(), member);
                possibleMembersByValue.put(member.getValue(), member);
            }
        }
    }

    @Override
    protected void onAdd(V group) {
        group.setParent(this);
    }

    @Override
    protected void onRemove(V group) {
        group.setParent(null);
    }

    @Override
    public void read(JsonElement jsonElement) {
        for(V value: values) onRemove(value);
        values.clear();

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for(JsonElement childElement: jsonArray) {
            V group = groupSupplier.get();
            onAdd(group);
            group.read(childElement);
            values.add(group);
        }

        onChange();
    }

    @Override
    public JsonElement write() {
        JsonArray jsonArray = new JsonArray();
        for(V group: values) jsonArray.add(group.write());
        return jsonArray;
    }

    public Optional<V> find(T key) {
        return stream().filter(it -> it.getMembers().contains(key)).findFirst();
    }

    public Set<IGroupMember<T>> getPossibleMembers() {
        return possibleMembers;
    }

    public GroupMember<T> getPossibleMemberById(String id) {
        return possibleMembersById.get(id);
    }

    public GroupMember<T> getPossibleMemberByValue(T key) {
        return possibleMembersByValue.get(key);
    }
}
