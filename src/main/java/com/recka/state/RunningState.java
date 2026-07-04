package com.recka.state;

public class RunningState implements TimerState {
    public void start(TimerContext context) { throw new IllegalStateException("Timer is already running."); }
    public void pause(TimerContext context) { context.setState(new PausedState()); }
    public void resume(TimerContext context) { throw new IllegalStateException("Timer is already running."); }
    public void stop(TimerContext context) { context.setState(new StoppedState()); }
    public String name() { return "RUNNING"; }
}
