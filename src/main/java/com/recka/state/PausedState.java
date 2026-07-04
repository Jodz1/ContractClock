package com.recka.state;

public class PausedState implements TimerState {
    public void start(TimerContext context) { throw new IllegalStateException("Timer is paused."); }
    public void pause(TimerContext context) { throw new IllegalStateException("Timer is already paused."); }
    public void resume(TimerContext context) { context.setState(new RunningState()); }
    public void stop(TimerContext context) { context.setState(new StoppedState()); }
    public String name() { return "PAUSED"; }
}
