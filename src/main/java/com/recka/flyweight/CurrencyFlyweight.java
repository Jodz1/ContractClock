package com.recka.flyweight;

public final class CurrencyFlyweight {
    private final String code;
    private final String symbol;
    CurrencyFlyweight(String code, String symbol) {
        this.code = code;
        this.symbol = symbol;
    }
    public String code() { return code; }
    public String symbol() { return symbol; }
}
