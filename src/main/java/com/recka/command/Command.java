package com.recka.command;

/** Command pattern: UI actions are wrapped as executable command objects. */
public interface Command {
    void execute() throws Exception;
}
