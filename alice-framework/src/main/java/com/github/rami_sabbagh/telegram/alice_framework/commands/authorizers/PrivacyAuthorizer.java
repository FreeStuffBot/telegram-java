package com.github.rami_sabbagh.telegram.alice_framework.commands.authorizers;

import com.github.rami_sabbagh.telegram.alice_framework.commands.Command;
import com.github.rami_sabbagh.telegram.alice_framework.commands.ParsedCommand;
import com.github.rami_sabbagh.telegram.alice_framework.commands.Privacy;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Determines if the commands privacy level allows executing it or not.
 */
public interface PrivacyAuthorizer extends Authorizer {

    /**
     * Checks if the message is from a bot admin.
     *
     * @param message The message to check.
     * @return {@code true} if it's from a bot admin, {@code false} otherwise.
     */
    boolean isAdmin(Message message);

    /**
     * Checks if the message is from a bot owner.
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

    /**
     * Checks if the command's privacy level allows using it.
     *
     * @param parsedCommand The parsed command request by the user.
     * @param command       The command implementation to execute.
     * @return {@code null} if it was valid, otherwise the rejection reason.
     */
    default String checkPrivacy(ParsedCommand parsedCommand, Command command) {
        Privacy privacy = command.privacy;
        Message source = parsedCommand.origin;

        switch (privacy) {
            case PUBLIC:
                return null; //Always authorized for anyone.
            case GROUP_ADMIN:
                if (isGroupAdmin(source)) return null;
            case ADMIN:
                if (isAdmin(source)) return null;
            case OWNER:
                if (isOwner(source)) return null;
                switch (privacy) {
                    case GROUP_ADMIN:
                        return "Only group admins are allowed to use this commands ⚠";
                    case ADMIN:
                        return "Only the bot staff are allowed to use this command ⚠";
                    case OWNER:
                        return "Only the bot owners are allowed to use this command ⚠";
                }
            default:
                new Exception("Unsupported command privacy level: " + command.privacy.name()).printStackTrace(); //TODO: Debug logging using SLF4J.
                return "An issue has occurred while executing the command ⚠";
        }
    }

    @Override
    default String authorize(ParsedCommand parsedCommand, Command command) {
        return checkPrivacy(parsedCommand, command);
    }
}
