package com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Command;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Locality;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.ParsedCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;

/**
 * Determines if the commands locality level allows executing it or not.
 */
public interface LocalityAuthorizer extends Authorizer {

    /**
     * Checks if this is a private chat.
     *
     * @param chat The chat to check.
     * @return {@code true} if it's a private chat.
     */
    default boolean isUserChat(Chat chat) {
        return chat.isUserChat();
    }

    /**
     * Checks if this is a group or a super group chat.
     *
     * @param chat The chat to check.
     * @return {@code true} if it's a group or super group chat.
     */
    default boolean isGroupChat(Chat chat) {
        return chat.isGroupChat() || chat.isSuperGroupChat();
    }

    /**
     * Checks if the command's locality level allows using it.
     *
     * @param parsedCommand The parsed command request by the user.
     * @param command       The command implementation to execute.
     * @return {@code null} if it was valid, otherwise the rejection reason.
     */
    default String checkLocality(ParsedCommand parsedCommand, Command command) {
        Locality locality = command.locality;
        Chat chat = parsedCommand.origin.getChat();

        switch (locality) {
            case ALL:
                return null;
            case USER:
                if (isUserChat(chat)) return null;
                return parsedCommand + " is available only in private chats ⚠";
            case GROUP:
                if (isGroupChat(chat)) return null;
                return parsedCommand + " is available only in groups ⚠";
            default:
                new Exception("Unsupported command locality level: " + locality.name()).printStackTrace(); //TODO: Debug logging using SLF4J.
                return "An issue has occurred while executing the command ⚠";
        }
    }

    @Override
    default String authorize(ParsedCommand parsedCommand, Command command) {
        return checkLocality(parsedCommand, command);
    }
}
