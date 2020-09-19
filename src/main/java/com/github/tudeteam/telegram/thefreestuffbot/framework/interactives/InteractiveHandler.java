package com.github.tudeteam.telegram.thefreestuffbot.framework.interactives;

import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.Handler;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the updates routing for the interactive handlers.
 */
public abstract class InteractiveHandler implements Handler<Update> {

    /**
     * Stores the registered interactive handlers
     */
    protected final Map<String, Interactive> handlers = new HashMap<>();

    /**
     * Registers an interactive handler.
     *
     * @param interactive The interactive handler to register.
     */
    public void registerInteractive(Interactive interactive) {
        assert !handlers.containsKey(interactive.id) : "There's an interactive handler under the same id '" + interactive.id + "'!";
        handlers.put(interactive.id, interactive);
    }

    /**
     * Unregisters an interactive a handler, so it's no longer processed.
     *
     * @param interactive The interactive handler to unregister.
     * @return {@code true} if the interactive handler was found and unregistered, {@code false} if it was not registered anyway.
     */
    public boolean unregisterInteractive(Interactive interactive) {
        return handlers.remove(interactive.id, interactive);
    }

    /**
     * Creates a new InteractiveBuilder which will automatically register the interactive when built.
     *
     * @return An InteractiveBuilder that will automatically register the command when built.
     */
    public InteractiveBuilder newHandler() {
        return new InteractiveBuilder() {
            @Override
            public Interactive build() {
                Interactive interactive = super.build();
                registerInteractive(interactive);
                return interactive;
            }
        };
    }

    /**
     * Sets the active handler for a chat.
     *
     * @param chatId        The chat handler.
     * @param handler       The handler to activate.
     * @param filterMessage The message to filter by it's id, can be null.
     * @param state         The state data, can be null for empty data.
     */
    public void activateHandler(long chatId, Interactive handler, Message filterMessage, Map<String, String> state) {
        setActiveHandlerId(chatId, handler.id);
        setMessageId(chatId, filterMessage == null ? null : filterMessage.getMessageId());
        setChatState(chatId, state);
    }

    /**
     * Deactivates the interactive handler for a chat.
     *
     * @param chatId The chat id.
     */
    public void deactivateHandler(long chatId) {
        setActiveHandlerId(chatId, null);
        setMessageId(chatId, null);
        setChatState(chatId, null);
    }

    /**
     * Gets the active interactive handler id for a chat.
     *
     * @param chatId The chat id.
     * @return The active interactive handler id, can be null.
     */
    protected abstract String getActiveHandlerId(long chatId);

    /**
     * Sets the active interactive handler id for a chat.
     *
     * @param chatId The chat id.
     * @param id     The active interactive handler id, can be null.
     */
    protected abstract void setActiveHandlerId(long chatId, String id);

    /**
     * Gets the interactivity state of the chat.
     *
     * @param chatId The chat id.
     * @return The interactivity state of the chat.
     */
    protected abstract Map<String, String> getChatState(long chatId);

    /**
     * Sets the interactivity state of the chat.
     *
     * @param chatId The chat id.
     * @param state  The interactivity state of the chat.
     */
    protected abstract void setChatState(long chatId, Map<String, String> state);

    /**
     * Gets the filtering message id of the chat.
     *
     * @param chatId The chat id.
     * @return The filtering message id, can be null.
     */
    protected abstract Integer getMessageId(long chatId);

    /**
     * Sets the filtering message id of the chat.
     *
     * @param chatId    The chat id.
     * @param messageId The filtering message id, can be null.
     */
    protected abstract void setMessageId(long chatId, Integer messageId);

    @Override
    public boolean process(Update event) {
        //Filter non-message updates.
        if (!event.hasMessage()) return false;

        Message message = event.getMessage();
        long chatId = message.getChatId();

        //The active handler id.
        String handlerId = getActiveHandlerId(chatId);
        Interactive handler = handlers.get(handlerId);

        //Unknown handler, ignore the message.
        if (handler == null) return false;

        //Filter by message id for ForceReply style interactivity.
        Integer messageId = getMessageId(chatId);
        if (messageId != null && (!message.isReply() || !message.getReplyToMessage().getMessageId().equals(messageId)))
            return false;

        //The interactivity state data for the chat.
        Map<String, String> state = getChatState(chatId);

        Message response = handler.process(message, state);

        if (response == Interactive.UNSET) {
            setChatState(chatId, null);
            setMessageId(chatId, null);
            setActiveHandlerId(chatId, null);
        } else {
            //Update the state data.
            setChatState(chatId, state);

            //Update the filtering message id.
            if (response == Interactive.ANY)
                setMessageId(chatId, null);
            else if (response != null)
                setMessageId(chatId, response.getMessageId());
            else
                return false; //Not consumed.
        }

        return true;
    }
}
