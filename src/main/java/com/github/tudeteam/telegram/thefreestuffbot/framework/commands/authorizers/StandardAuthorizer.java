package com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers;

import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * A {@code BasicAuthorizer} which has a SilentExecutor for requesting extra information about it's users.
 * <p>
 * It has the isGroupAdmin method implemented by requesting the {@code ChatMember} data about the user.
 */
public abstract class StandardAuthorizer implements BasicAuthorizer {

    /**
     * The SilentExecutor used for looking up more information about the users in the authorization process.
     */
    protected final SilentExecutor silent;

    /**
     * Constructs a new instance.
     *
     * @param silent The SilentExecutor used for looking up more information about the users in the authorization process.
     */
    public StandardAuthorizer(SilentExecutor silent) {
        this.silent = silent;
    }

    @Override
    public boolean isGroupAdmin(Message message) {
        if (isUserChat(message.getChat())) return true; //Users are group admins of their private chats.

        ChatMember member = silent.execute(new GetChatMember()
                .setChatId(message.getChatId())
                .setUserId(message.getFrom().getId()));
        if (member == null) return false;

        return member.getStatus().equals("administrator") || member.getStatus().equals("creator");
    }
}
