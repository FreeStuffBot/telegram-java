package com.github.tudeteam.telegram.thefreestuffbot.framework.commands;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Represents a command which can be executed by the bot.
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
     * @param name        The name of the command, ex: {@code "start"} for <i>/start</i>.
     * @param description The description of the command, can be null for no description.
     * @param locality    The availability of the command by chat type.
     * @param privacy     The availability of the command by the user permissions level.
     * @throws NullPointerException when one of the non-optional parameters is null.
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
     * @param source        The message which triggered the command execution.
     * @param parsedCommand The parsed command content from the message.
     * @throws TelegramApiException Telegram API exceptions.
     */
    public abstract void action(Message source, ParsedCommand parsedCommand) throws TelegramApiException;

}
