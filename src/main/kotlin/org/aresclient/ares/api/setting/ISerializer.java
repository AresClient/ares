package org.aresclient.ares.api.setting;

public interface ISerializer<R> {
    Setting<?> read(Setting.ReadInfo readInfo, R data/*nullable*/);
    R write(Setting<?> setting);
}
