package com.recka.state;

public class IdleState implements TimerState {
    public void start(TimerContext context) { context.setState(new RunningState()); }
    public void pause(TimerContext context) { throw new IllegalStateException("Timer is idle."); }
    public void resume(TimerContext context) { throw new IllegalStateException("Timer is idle."); }
    public void stop(TimerContext context) { context.setState(new StoppedState()); }
    public String name() { return "IDLE"; }
}
