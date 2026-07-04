package com.recka.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private DateTimeUtil() {}

    public static String formatDuration(long seconds) {
        long abs = Math.max(0, seconds);
        long hours = abs / 3600;
        long minutes = (abs % 3600) / 60;
        long sec = abs % 60;
        if (hours > 0) return String.format("%dh %02dm", hours, minutes);
        return String.format("%dm %02ds", minutes, sec);
    }

    public static String formatTimer(Duration duration) {
        long seconds = Math.max(0, duration.getSeconds());
        return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    public static String safeDate(LocalDate date) {
        return date == null ? "-" : DATE.format(date);
    }

    public static String safeDateTime(LocalDateTime dt) {
        return dt == null ? "-" : DATE_TIME.format(dt);
    }
}
