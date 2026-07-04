package com.recka.event;

import java.time.LocalDateTime;

public abstract class AppEvent {
    private final LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime getCreatedAt() { return createdAt; }
}
