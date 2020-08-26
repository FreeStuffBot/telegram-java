package com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers;

import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * A {@code StandardAuthorizer} which doesn't accept {@code ADMIN} and {@code OWNER} commands.
 */
public class DefaultAuthorizer extends StandardAuthorizer {

    /**
     * Constructs a new instance.
     *
     * @param silent The silent executor to use for requesting more information about the users.
     */
    public DefaultAuthorizer(SilentExecutor silent) {
        super(silent);
    }

    @Override
    public boolean isAdmin(Message message) {
        return false;
    }

    @Override
    public boolean isOwner(Message message) {
        return false;
    }
}
