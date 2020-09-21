package com.github.rami_sabbagh.telegram.alice_framework.interactivity;

import com.github.rami_sabbagh.telegram.alice_framework.pipes.Handler;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public abstract class InteractivityHandler implements Handler<Update> {

    protected Map<String, InteractivityListener> listeners = new HashMap<>();

    public void registerListener(String listenerId, InteractivityListener listener) {
        assert !listeners.containsKey(listenerId) : "A listener is already registered under the same id '" + listenerId + "'!";
        listeners.put(listenerId, listener);
    }

    public void activateListener(long chatId, String listenerId) {
        activateListener(chatId, listenerId, new HashMap<>());
    }

    public void activateListener(long chatId, String listenerId, Map<String, String> initData) {
        assert listeners.containsKey(listenerId) : "Unknown listener '" + listenerId + "'!";

        //Deactivate the previous listener if there is one.
        deactivateListener(chatId);

        InteractivityState state = new InteractivityState(initData, null);
        InteractivityListener listener = listeners.get(listenerId);

        listener.activated(chatId, state);

        //Check if the listener didn't accept the job.
        if (state.finished) return;

        //Record the listener as activated.
        setActiveListenerId(chatId, listenerId);
        setMessageId(chatId, state.botMessageId);
        setStateData(chatId, state.data);
    }

    /**
     * Deactivates the active listener for a chat.
     *
     * @param chatId The chat id.
     * @return {@code true} If a listener was deactivate, {@code false} if there was no listener activated for the chat.
     */
    public boolean deactivateListener(long chatId) {
        String listenerId = getActiveListenerId(chatId);
        if (listenerId == null) return false;

        InteractivityListener listener = listeners.get(listenerId);
        if (listener != null) {
            InteractivityState state = new InteractivityState(getStateData(chatId), getMessageId(chatId));
            listener.deactivated(chatId, state);
        }

        //Wipe the data of the listener.
        setActiveListenerId(chatId, null);
        setMessageId(chatId, null);
        setStateData(chatId, null);

        return true;
    }

    protected abstract void setActiveListenerId(long chatId, String listenerId);

    protected abstract String getActiveListenerId(long chatId);

    protected abstract void setMessageId(long chatId, Integer messageId);

    protected abstract Integer getMessageId(long chatId);

    protected abstract void setStateData(long chatId, Map<String, String> data);

    protected abstract Map<String, String> getStateData(long chatId);

    @Override
    public boolean process(Update event) {
        //Filter non-messages updates.
        if (!event.hasMessage()) return false;

        Message message = event.getMessage();
        long chatId = message.getChatId();

        //Get the active listener id.
        String listenerId = getActiveListenerId(chatId);

        //No active listener for this chat.
        if (listenerId == null) return false;

        //Get the listener.
        InteractivityListener listener = listeners.get(listenerId);

        //Unknown listener, ignore the message.
        if (listener == null) return false;

        //Filter by message id if set.
        Integer messageId = getMessageId(chatId);
        if (messageId != null && (!message.isReply() || !message.getReplyToMessage().getMessageId().equals(messageId)))
            return false;

        //Get the state data of the handler.
        Map<String, String> data = getStateData(chatId);

        //Construct the interactivity state.
        InteractivityState state = new InteractivityState(data, messageId);

        //Trigger the listener.
        boolean consumed = listener.process(chatId, message, state);

        //Check if the listener decided to quit work.
        if (state.finished) {
            //If so, wipe his data.
            setActiveListenerId(chatId, null);
            setMessageId(chatId, null);
            setStateData(chatId, null);
        } else {
            //Otherwise update his data.
            setMessageId(chatId, state.botMessageId);
            setStateData(chatId, state.data);
        }

        //Report if the message was consumed or not.
        return consumed;
    }
}
