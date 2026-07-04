package com.recka.state;

public interface TimerState {
    void start(TimerContext context);
    void pause(TimerContext context);
    void resume(TimerContext context);
    void stop(TimerContext context);
    String name();
}
