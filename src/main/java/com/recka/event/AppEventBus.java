package com.recka.event;

import javafx.application.Platform;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Singleton + Observer pattern: UI subscribes to domain events and refreshes in real time. */
public final class AppEventBus {
    private static final AppEventBus INSTANCE = new AppEventBus();
    private final List<Consumer<AppEvent>> subscribers = new CopyOnWriteArrayList<>();

    private AppEventBus() {}

    public static AppEventBus getInstance() { return INSTANCE; }

    public void subscribe(Consumer<AppEvent> subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(Consumer<AppEvent> subscriber) {
        subscribers.remove(subscriber);
    }

    public void publish(AppEvent event) {
        for (Consumer<AppEvent> subscriber : subscribers) {
            if (Platform.isFxApplicationThread()) subscriber.accept(event);
            else Platform.runLater(() -> subscriber.accept(event));
        }
    }
}
