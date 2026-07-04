package com.recka.state;

/** State pattern: the timer cannot move through invalid transitions silently. */
public class TimerContext {
    private TimerState state = new IdleState();
    public TimerState getState() { return state; }
    void setState(TimerState state) { this.state = state; }
    public void start() { state.start(this); }
    public void pause() { state.pause(this); }
    public void resume() { state.resume(this); }
    public void stop() { state.stop(this); }
    public boolean isRunning() { return state instanceof RunningState; }
    public boolean isPaused() { return state instanceof PausedState; }
}
