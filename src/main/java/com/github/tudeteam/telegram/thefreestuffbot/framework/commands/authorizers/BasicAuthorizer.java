package com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Command;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * The basic authorizer base.
 */
public interface BasicAuthorizer extends Authorizer {

    /**
     * Checks if the message is from an admin of the bot.
     *
     * @param message The message to check.
     * @return {@code true} if it's from a bot admin, {@code false} otherwise.
     */
    boolean isAdmin(Message message);

    /**
     * Checks if the message is from an owner of the bot.
     *
     * @param message The message to check.
     * @return {@code true} if it's from a bot owner, {@code false} otherwise.
     */
    boolean isOwner(Message message);

    /**
     * Checks if the message is from a group admin.
     *
     * @param message The message from the user to check.
     * @return {@code true} if it's from a group admin, {@code false} otherwise.
     */
    boolean isGroupAdmin(Message message);

    @Override
    default String authorize(Message source, Command command) {
        switch (command.privacy) {
            case PUBLIC:
                return null; //Always authorized for anyone.
            case GROUP_ADMIN:
                if (isGroupAdmin(source)) return null;
            case ADMIN:
                if (isAdmin(source)) return null;
            case OWNER:
                if (isOwner(source)) return null;
                switch (command.privacy) {
                    case GROUP_ADMIN:
                        return "Only group admins are allowed to use this commands ⚠";
                    case ADMIN:
                        return "Only the bot staff are allowed to use this command ⚠";
                    case OWNER:
                        return "Only the bot owners are allowed to use this command ⚠";
                }
            default:
                new Exception("Unsupported command privacy level: " + command.privacy.name()).printStackTrace();
                return "A problem has happened while executing the command ⚠";
        }
    }
}
