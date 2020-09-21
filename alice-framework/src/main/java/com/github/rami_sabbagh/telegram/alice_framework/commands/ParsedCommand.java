package com.github.rami_sabbagh.telegram.alice_framework.commands;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

/**
 * Represents a parsed command message from a user.
 * <p>
 * {@code ParsedCommand} is commands requested by users, constructed from Telegram updates.
 * <p>
 * {@code Command} is an executable command provided by the bot, constructed at the bot's initialization.
 */
public class ParsedCommand {
    /**
     * The name of the command, ex: {@code "ping"}.
     */
    public final String name;

    /**
     * The username included in the message, null if absent, ex: <i>MyBot</i> for <i>/ping@MyBot</i>.
     */
    public final String username;

    /**
     * Any text present after the command, with whitespace trimmed, can be null, ex: <i>data</i> for <i>/ping@MyBot data</i>.
     */
    public final String parameters;

    /**
     * The origin message of the command.
     */
    public final Message origin;

    /**
     * Constructs a new instance.
     *
     * @param name       The name of the command, ex: {@code "ping"}.
     * @param username   The username included in the message, null if absent, ex: <i>MyBot</i> for <i>/ping@MyBot</i>.
     * @param parameters Any text present after the command, with whitespace trimmed, can be null, ex: <i>data</i> for <i>/ping@MyBot data</i>.
     * @param origin     The origin message of the command.
     */
    protected ParsedCommand(String name, String username, String parameters, Message origin) {
        this.name = name;
        this.username = username;
        this.parameters = parameters;
        this.origin = origin;
    }

    /**
     * Parses a command from a message.
     *
     * @param message The command message to parse.
     * @return The parsed command.
     */
    public static ParsedCommand parse(Message message) {
        assert message.isCommand() : "Not a command message!";
        MessageEntity commandEntity = message.getEntities().get(0);

        String commandTag = message.getText().substring(1, commandEntity.getLength());
        String commandName, commandUsername;

        int atLocation = commandTag.indexOf('@');
        if (atLocation == -1) {
            commandName = commandTag;
            commandUsername = null;
        } else {
            commandName = commandTag.substring(0, atLocation);
            commandUsername = commandTag.substring(atLocation + 1);
        }

        String parameters = message.getText().substring(commandEntity.getLength()).trim();

        return new ParsedCommand(commandName, commandUsername, parameters, message);
    }

    /**
     * Formats the command tag only.
     *
     * @return the command tag only (without the parameters).
     */
    @Override
    public String toString() {
        if (username == null)
            return "/" + name;
        else
            return "/" + name + "@" + username;
    }
}
