package dev.tigr.ares.core.util;

/**
 * @author Makrennel 6/9/21
 * @param <A> first type
 * @param <B> second type
 */
public class TimedPair<A, B> extends Pair<A, B> {
    private Timer timer;

    public TimedPair(A first, B second) {
        super(first, second);
        this.timer = new Timer();
    }

    public TimedPair(Pair<A, B> pair) {
        super(pair.getFirst(), pair.getSecond());
        this.timer = new Timer();
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }
}
