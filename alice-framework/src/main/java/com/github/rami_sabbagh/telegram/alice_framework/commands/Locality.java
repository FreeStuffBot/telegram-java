package com.github.rami_sabbagh.telegram.alice_framework.commands;

/**
 * Represents where the command can be used.
 */
public enum Locality {

    /**
     * Can be used anywhere (except channels).
     */
    ALL,

    /**
     * Can be used only in private messages.
     */
    USER,

    /**
     * Can be used only in groups.
     */
    GROUP
}
