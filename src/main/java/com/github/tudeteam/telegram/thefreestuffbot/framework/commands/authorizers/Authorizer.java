package com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Command;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.ParsedCommand;

/**
 * An authorizer for commands execution by specific users.
 */
public interface Authorizer {
    /**
     * Authorize the usage of a command.
     *
     * @param parsedCommand The command request parsed.
     * @param command       The command under execution.
     * @return {@code null} if the request is authorized, otherwise the reason why it was rejected.
     */
    String authorize(ParsedCommand parsedCommand, Command command);
}
