package com.github.rami_sabbagh.telegram.alice_framework.interactivity;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

public class InteractivityState {
    public final Map<String, String> data;
    public Integer botMessageId;
    public boolean finished;

    public InteractivityState(Map<String, String> data, Integer botMessageId) {
        this.data = data;
        this.botMessageId = botMessageId;
    }

    public boolean setFilterInGroups(Message message) {
        if (!message.isUserMessage())
            botMessageId = message.getMessageId();
        return true;
    }
}
