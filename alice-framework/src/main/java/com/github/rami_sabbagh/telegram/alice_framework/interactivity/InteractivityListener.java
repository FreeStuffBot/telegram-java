package com.github.rami_sabbagh.telegram.alice_framework.interactivity;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface InteractivityListener {

    /**
     * Called when the listener is activated on a chat.
     *
     * @param chatId The chat id.
     * @param state  The listener state (Modifiable).
     */
    void activated(long chatId, InteractivityState state);

    /**
     * Called when a message is recieved for the listener to process.
     *
     * @param chatId  The chat id.
     * @param message The message to process.
     * @param state   The listener state (Modifiable).
     * @return {@code true} when the message is consumed, {@code false} otherwise.
     */
    boolean process(long chatId, Message message, InteractivityState state);

    /**
     * Called when the listener is deactivate on a chat (in some way, but not by itself).
     *
     * @param chatId The chat id.
     * @param state  The listener state (Modifiable) (finished field is ignored here).
     */
    void deactivated(long chatId, InteractivityState state);
}
