package com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Command;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * An authorizer for commands execution by specific users.
 */
public interface Authorizer {
    /**
     * Authorize the usage of a command.
     *
     * @param source  The source of the command request.
     * @param command The command under execution.
     * @return {@code null} if the request is authorized, otherwise the reason why it was rejected.
     */
    String authorize(Message source, Command command);
}
