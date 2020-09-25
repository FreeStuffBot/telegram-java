package com.github.tudeteam.telegram.thefreestuffbot.structures;

/**
 * Represents the type of currency to display in announcements.
 */
public enum Currency {
    USD("$"),
    EUR("€");

    private final String symbol;

    Currency(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
