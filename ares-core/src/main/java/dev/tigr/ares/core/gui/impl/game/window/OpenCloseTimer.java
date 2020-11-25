package dev.tigr.ares.core.gui.impl.game.window;

/**
 * Provides basic functions and timings for linear two way open/close animation
 *
 * @author Tigermouthbear 6/26/20
 */
public class OpenCloseTimer {
    /**
     * Stores length of the animation
     */
    private final int length;
    /**
     * Stores whether the timer is open or not
     */
    private boolean state;
    /**
     * Stores the previous animation for use in getting the factor
     */
    private Animation prevAnimation = Animation.NONE;
    /**
     * Stores the current animation which the timer is on
     */
    private Animation animation = Animation.NONE;
    /**
     * Stores the time which the current animation was started on
     */
    private long animationStart = 0;

    /**
     * Constructor for a two way animation timer
     *
     * @param length the length of both animations
     */
    public OpenCloseTimer(int length, boolean state) {
        this.length = length;
        this.state = state;

        // set previous animation to fix bug
        if(state) prevAnimation = Animation.OPENING;
    }

    /**
     * Ticks the timer. If the timer is up, stop timing
     */
    public void tick() {
        if(animation != Animation.NONE) {
            if(System.currentTimeMillis() - animationStart > length) {
                prevAnimation = animation;
                animation = Animation.NONE;
                if(prevAnimation == Animation.CLOSING) state = false;
            }
        }
    }

    /**
     * Gets a double between 0 and 1 which is calculated by the animation time
     *
     * @return factor used for animating, ranges 0 through 1
     */
    public double getAnimationFactor() {
        if(animation == Animation.OPENING) return (System.currentTimeMillis() - animationStart) / (double) length;
        if(animation == Animation.CLOSING)
            return ((long) length - (System.currentTimeMillis() - animationStart)) / (double) length;
        return prevAnimation == Animation.OPENING ? 1 : 0;
    }

    /**
     * Getter for state of timer
     *
     * @return whether the timer is open or closed
     */
    public boolean getState() {
        return state;
    }

    /**
     * Sets animation based on boolean value, true for opening false for closing
     *
     * @param value boolen value to base off
     */
    public void setState(boolean value) {
        // set animation and state
        if(value) {
            animation = Animation.OPENING;
            state = true;
        } else animation = Animation.CLOSING;

        // start timer
        animationStart = System.currentTimeMillis();
    }

    /**
     * Toggles the state of the timer
     */
    public void toggle() {
        setState(!getState());
    }

    /**
     * Enums for animation state
     */
    enum Animation {OPENING, CLOSING, NONE}
}
