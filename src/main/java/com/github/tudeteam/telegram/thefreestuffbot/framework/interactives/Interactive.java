package com.github.tudeteam.telegram.thefreestuffbot.framework.interactives;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

/**
 * Represents an "interactive" handler for messages.
 */
public abstract class Interactive {

    /**
     * Can be returned by {@code process} to indicate that the interactive handler can be triggered
     * for any message sent in the chat.
     */
    public static final Message ANY = new Message();

    /**
     * Can be returned by {@code process} to indicate that the interactive handler has to be UNSET for that chat.
     */
    public static final Message UNSET = new Message();

    /**
     * The id of the interactive handler for representation in the database.
     */
    public final String id;

    /**
     * Constructs a new interactive handler with the specific id.
     * <p>
     * The id should be unique and not conflict with other handlers.
     *
     * @param id The id of the interactive handler to store in the database.
     */
    public Interactive(String id) {
        this.id = id;
    }

    /**
     * Invoked when the a message has to be processed by this interactive handler.
     *
     * @param message The user's message to be processed.
     * @param state   A map for storing the state data for the chat, can be an empty map when the handler is set new.
     * @return A message sent by the bot to only process the replies sent to it
     * {@code null} to indicate that the message was not processed (ignored), keeps the message filter as it is.
     * {@code ANY} to set the messages filter to any messages.
     * {@code UNSET} to unset the interactive handler for the chat.
     */
    public abstract Message process(Message message, Map<String, String> state);
}
