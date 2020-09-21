package com.github.rami_sabbagh.telegram.alice_framework.commands;

import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Represents a command which can be executed by the bot.
 * <p>
 * {@code Command} is an executable command provided by the bot, constructed at the bot's initialization.
 * <p>
 * {@code ParsedCommand} is commands requested by users, constructed from Telegram updates.
 */
public abstract class Command {

    /**
     * The name of the command, ex: {@code "start"} for <i>/start</i>.
     */
    public final String name;

    /**
     * The description of the command, can be null for no description.
     */
    public final String description;

    /**
     * The availability of the command by chat type.
     */
    public final Locality locality;

    /**
     * The availability of the command by the user permissions level.
     */
    public final Privacy privacy;

    /**
     * Constructs an instance of the command.
     *
     * @param name        The name of the command, ex: {@code "start"} for <i>/start</i>. Required.
     * @param description The description of the command, can be {@code null} for no description. Optional.
     * @param locality    The availability of the command by chat type. Required.
     * @param privacy     The availability of the command by the user permissions level. Required.
     * @throws NullPointerException when one of the non-optional parameters is null. Required.
     */
    public Command(String name, String description, Locality locality, Privacy privacy) throws NullPointerException {
        if (name == null) throw new NullPointerException("name can't be null!");
        if (locality == null) throw new NullPointerException("locality can't be null!");
        if (privacy == null) throw new NullPointerException("privacy can't be null!");

        this.name = name;
        this.description = description;
        this.locality = locality;
        this.privacy = privacy;
    }

    /**
     * Executes the command.
     *
     * @param message       The message which triggered the command execution.
     * @param parsedCommand The parsed command content from the message.
     */
    public abstract void action(Message message, ParsedCommand parsedCommand);

}
