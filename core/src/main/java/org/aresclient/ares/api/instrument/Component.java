package org.aresclient.ares.api.instrument;

import dev.tigr.simpleevents.event.Event;
import dev.tigr.simpleevents.listener.EventHandler;
import dev.tigr.simpleevents.listener.EventListener;
import org.aresclient.ares.api.setting.Setting;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Using components can help to break up a larger modules into smaller
 * module-specific utilities for ease of code readability and navigability.
 *
 * @param <I> The module a particular component pertains to
 */
public class Component<I extends Instrument> {
    public I master;

    public Component(I master) {
        this.master = master;
        this.master.addComponent(this);
    }

    public static class Func<I extends Instrument> extends Component<I> {
        private final Runnable function;

        public Func(I master, Runnable function) {
            super(master);
            this.function = function;
        }

        public void run() {
            function.run();
        }
    }

    public static class Returnable<M extends Instrument, T> extends Component<M> {
        private final Supplier<T> returnableFunction;

        public Returnable(M master, Supplier<T> returnableFunction) {
            super(master);
            this.returnableFunction = returnableFunction;
        }

        public T get() {
            return returnableFunction.get();
        }
    }

    public static class Parameterized<I extends Instrument, T> extends Component<I> {
        private final Consumer<T> parameterizedFunction;

        public Parameterized(I master, Consumer<T> parameterizedFunction) {
            super(master);
            this.parameterizedFunction = parameterizedFunction;
        }

        public void run(T parameter) {
            parameterizedFunction.accept(parameter);
        }
    }

    public static class Listener<I extends Instrument, T extends Event> extends Component<I> {
        @EventHandler
        private final EventListener<T> eventListener;

        public Listener(I master, EventListener<T> eventListener) {
            super(master);
            this.eventListener = eventListener;
        }
    }

    public static class Settings<I extends Instrument> extends Component<I> {
        private final String pathName;
        private Setting.Map<?> settings;

        protected Settings(I master, String pathName) {
            super(master);
            this.pathName = pathName;
        }

        protected Setting.Map<?> getSettings() {
            return settings;
        }

        void setSettings(Setting.Map<?> settings) {
            this.settings = settings;
        }

        String getPathName() {
            return pathName;
        }
    }
}
