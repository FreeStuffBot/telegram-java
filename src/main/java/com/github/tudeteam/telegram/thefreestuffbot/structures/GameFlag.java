package com.github.tudeteam.telegram.thefreestuffbot.structures;

public enum GameFlag {
    /**
     * Low quality game.
     */
    TRASH(0b0000_0001),

    /**
     * Third party key provider.
     */
    THIRDPARTY(0b0000_0010);

    /* Enum code */

    private final int bitmask;

    GameFlag(int bitmask) {
        this.bitmask = bitmask;
    }

    /**
     * Gets the bitmask value of the enum.
     *
     * @return The bitmask value of the enum.
     */
    public int bitmask() {
        return bitmask;
    }

    /**
     * Checks if the flag is set in the provided bits bundle.
     *
     * @param bits The bits bundle.
     * @return {@code true} if set, {@code false} otherwise.
     */
    public boolean isSet(int bits) {
        return (bits & bitmask) != 0;
    }
}
