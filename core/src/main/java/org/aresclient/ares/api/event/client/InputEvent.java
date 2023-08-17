package org.aresclient.ares.api.event.client;

import org.aresclient.ares.api.event.AresEvent;

public class InputEvent extends AresEvent {
    public enum Type {
        KEYBOARD,
        MOUSE
    }

    private final Type type;

    public InputEvent(Type type) {
        super("input_" + type.name(), null);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public static class Keyboard extends InputEvent {
        public enum State {
            PRESSED,
            RELEASED
        }

        private final State state;
        private final int key;

        public Keyboard(State state, int key) {
            super(Type.KEYBOARD);
            this.state = state;
            this.key = key;
        }

        public State getState() {
            return state;
        }

        public int getKey() {
            return key;
        }

        public static class Pressed extends Keyboard {
            public Pressed(int key) {
                super(State.PRESSED, key);
            }
        }

        public static class Released extends Keyboard {
            public Released(int key) {
                super(State.RELEASED, key);
            }
        }
    }

    public static class Mouse extends InputEvent {
        public enum State {
            PRESSED,
            RELEASED,
            SCROLLED,
            MOVED
        }

        private final State state;

        public Mouse(State state) {
            super(Type.MOUSE);
            this.state = state;
        }

        public State getState() {
            return state;
        }

        public static class Pressed extends Mouse {
            private final int key;

            public Pressed(int key) {
                super(State.PRESSED);
                this.key = key;
            }

            public int getKey() {
                return key;
            }
        }

        public static class Released extends Mouse {
            private final int key;

            public Released(int key) {
                super(State.RELEASED);
                this.key = key;
            }

            public int getKey() {
                return key;
            }
        }

        public static class Scrolled extends Mouse {
            private final double vertical;

            public Scrolled(double vertical) {
                super(State.SCROLLED);
                this.vertical = vertical;
            }

            public double getVertical() {
                return vertical;
            }
        }

        public static class Moved extends Mouse {
            private final double x;
            private final double y;
            private final double deltaX;
            private final double deltaY;

            public Moved(double x, double y, double deltaX, double deltaY) {
                super(State.MOVED);
                this.x = x;
                this.y = y;
                this.deltaX = deltaX;
                this.deltaY = deltaY;
            }

            public double getX() {
                return x;
            }

            public double getY() {
                return y;
            }

            public double getDeltaX() {
                return deltaX;
            }

            public double getDeltaY() {
                return deltaY;
            }
        }
    }
}
