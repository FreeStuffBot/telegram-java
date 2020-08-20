package com.github.tudeteam.telegram.thefreestuffbot.framework.commands;

/**
 * The availability of the command by the user permissions level.
 */
public enum Privacy {
    /**
     * The command can be used by anyone.
     */
    PUBLIC,

    /**
     * The command can be used only by group admins.
     */
    GROUP_ADMIN,

    /**
     * The command can be only used by a bot admin.
     */
    ADMIN,

    /**
     * The command can be used only by the owners of the bot.
     */
    OWNER
}
