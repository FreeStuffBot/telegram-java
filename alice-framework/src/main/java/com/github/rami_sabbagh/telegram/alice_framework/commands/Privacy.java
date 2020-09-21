package com.github.rami_sabbagh.telegram.alice_framework.commands;

/**
 * Represents who can use the command.
 */
public enum Privacy {

    /**
     * Can be used by anyone.
     */
    PUBLIC,

    /**
     * Can be used only by group admins, bot admins and bot owners.
     */
    GROUP_ADMIN,

    /**
     * Can be only used by bot admins and bot owners.
     */
    ADMIN,

    /**
     * Can be used only by the bot owners.
     */
    OWNER
}
