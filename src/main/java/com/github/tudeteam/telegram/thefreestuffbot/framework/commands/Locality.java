package com.github.tudeteam.telegram.thefreestuffbot.framework.commands;

/**
 * The availability of the command by chat type.
 */
public enum Locality {
    /**
     * The command can be used anywhere (except channels).
     */
    ALL,

    /**
     * The command can be only used in private messages.
     */
    USER,

    /**
     * The command can be only used in groups.
     */
    GROUP
}
