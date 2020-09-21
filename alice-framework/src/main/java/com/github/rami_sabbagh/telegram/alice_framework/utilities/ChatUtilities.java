package com.github.rami_sabbagh.telegram.alice_framework.utilities;

import org.telegram.telegrambots.meta.api.objects.Chat;

public class ChatUtilities {

    /**
     * Gets the type of a chat.
     * @param chat The chat to check.
     * @return The type of the chat.
     */
    public static ChatType getChatType(Chat chat) {
        if (chat.isUserChat()) return ChatType.USER;
        else if (chat.isGroupChat()) return ChatType.GROUP;
        else if (chat.isSuperGroupChat()) return ChatType.SUPERGROUP;
        else if (chat.isChannelChat()) return ChatType.CHANNEL;
        else return ChatType.UNKNOWN;
    }

    public enum ChatType {
        USER, GROUP, SUPERGROUP, CHANNEL, UNKNOWN
    }
}
