package com.recka.state;

public class StoppedState implements TimerState {
    public void start(TimerContext context) { context.setState(new RunningState()); }
    public void pause(TimerContext context) { throw new IllegalStateException("Timer is stopped."); }
    public void resume(TimerContext context) { throw new IllegalStateException("Timer is stopped."); }
    public void stop(TimerContext context) { throw new IllegalStateException("Timer is already stopped."); }
    public String name() { return "STOPPED"; }
}
