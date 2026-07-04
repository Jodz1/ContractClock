package com.recka.flyweight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Flyweight + Factory pattern: shared immutable UI/domain metadata is cached and reused. */
public final class FlyweightFactory {
    private static final FlyweightFactory INSTANCE = new FlyweightFactory();
    private final Map<String, CurrencyFlyweight> currencies = new ConcurrentHashMap<>();
    private final Map<String, ActivityTagFlyweight> tags = new ConcurrentHashMap<>();
    private final Map<String, StatusFlyweight> statuses = new ConcurrentHashMap<>();

    private FlyweightFactory() {}
    public static FlyweightFactory getInstance() { return INSTANCE; }

    public CurrencyFlyweight currency(String code) {
        String normalized = code == null || code.isBlank() ? "USD" : code.toUpperCase();
        return currencies.computeIfAbsent(normalized, c -> new CurrencyFlyweight(c, symbolFor(c)));
    }

    public ActivityTagFlyweight activityTag(String name, String colorHex) {
        String key = (name == null ? "General" : name) + "|" + (colorHex == null ? "#64748B" : colorHex);
        return tags.computeIfAbsent(key, k -> new ActivityTagFlyweight(name == null ? "General" : name, colorHex == null ? "#64748B" : colorHex));
    }

    public StatusFlyweight status(String status) {
        String key = status == null ? "ACTIVE" : status.toUpperCase();
        return statuses.computeIfAbsent(key, k -> switch (k) {
            case "ACTIVE" -> new StatusFlyweight(k, "Active", "#10B981");
            case "PAUSED" -> new StatusFlyweight(k, "Paused", "#F59E0B");
            case "COMPLETED" -> new StatusFlyweight(k, "Completed", "#6366F1");
            case "ARCHIVED" -> new StatusFlyweight(k, "Archived", "#64748B");
            default -> new StatusFlyweight(k, k, "#64748B");
        });
    }

    private String symbolFor(String code) {
        return switch (code) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "RSD" -> "RSD";
            default -> code;
        };
    }
}
