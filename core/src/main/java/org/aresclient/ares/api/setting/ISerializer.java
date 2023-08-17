package org.aresclient.ares.api.setting;

public interface ISerializer<T> {
    <R> void read(Setting<R, T> setting, T data);

    <R> T write(Setting<R, T> setting);
}
