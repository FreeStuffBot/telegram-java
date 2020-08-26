package com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers;

import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * A {@code StandardAuthorizer} which authorizes a specific "Bot Owner" for {@code OWNER} and {@code ADMIN} commands.
 */
public class AuthorizeOwner extends StandardAuthorizer {

    /**
     * The Telegram userId of the bot's owner.
     */
    protected final int ownerId;

    /**
     * Constructs a new instance.
     *
     * @param silent  The silent executor to use for requesting more information about the users.
     * @param ownerId The Telegram userId of the bot's owner.
     */
    public AuthorizeOwner(SilentExecutor silent, int ownerId) {
        super(silent);
        this.ownerId = ownerId;
    }

    @Override
    public boolean isOwner(Message message) {
        return message.getFrom().getId() == ownerId;
    }

    @Override
    public boolean isAdmin(Message message) {
        return false;
    }
}
