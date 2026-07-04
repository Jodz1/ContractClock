package com.recka.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {
    private MoneyUtil() {}

    public static BigDecimal calculateAmount(long durationSeconds, BigDecimal hourlyRate) {
        if (hourlyRate == null) hourlyRate = BigDecimal.ZERO;
        BigDecimal hours = BigDecimal.valueOf(durationSeconds)
                .divide(BigDecimal.valueOf(3600), 8, RoundingMode.HALF_UP);
        return hours.multiply(hourlyRate).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal finalAmount(boolean chargeable, BigDecimal calculatedAmount, BigDecimal overrideAmount) {
        if (!chargeable) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (overrideAmount != null) return overrideAmount.setScale(2, RoundingMode.HALF_UP);
        if (calculatedAmount == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return calculatedAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public static String format(BigDecimal amount, String currency) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        return (currency == null ? "USD" : currency) + " " + value.setScale(2, RoundingMode.HALF_UP);
    }
}
