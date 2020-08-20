package com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers;

import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * An authorizer which authorizes a single owner for non-public commands.
 * Public commands are authorized for everyone.
 */
public class AuthorizeOwner implements BasicAuthorizer {

    public final int ownerId;

    /**
     * Create an authorizer of a single owner.
     *
     * @param ownerId The Telegram id of the owner user.
     */
    public AuthorizeOwner(int ownerId) {
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

    @Override
    public boolean isGroupAdmin(Message message) {
        return false;
    }
}
