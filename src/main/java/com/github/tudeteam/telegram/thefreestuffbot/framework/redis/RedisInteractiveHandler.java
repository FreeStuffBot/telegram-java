package com.github.tudeteam.telegram.thefreestuffbot.framework.redis;

import com.github.tudeteam.telegram.thefreestuffbot.framework.interactives.InteractiveHandler;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.HashMap;
import java.util.Map;

public class RedisInteractiveHandler extends InteractiveHandler {

    protected final String keyPrefix;
    protected final RedisCommands<String, String> commands;

    /**
     * Creates a new Interactive handler which operates on the redis database.
     *
     * @param namespace The namespace to use for the redis keys.
     * @param commands  The redis sync commands object.
     */
    public RedisInteractiveHandler(String namespace, RedisCommands<String, String> commands) {
        keyPrefix = namespace + ":interactive:";
        this.commands = commands;
    }

    @Override
    protected String getActiveHandlerId(long chatId) {
        return commands.get(keyPrefix + chatId + ":handlerId");
    }

    @Override
    protected void setActiveHandlerId(long chatId, String id) {
        if (id == null)
            commands.del(keyPrefix + chatId + ":handlerId");
        else
            commands.set(keyPrefix + chatId + ":handlerId", id);
    }

    @Override
    protected Map<String, String> getChatState(long chatId) {
        Map<String, String> state = new HashMap<>();
        Map<String, String> map = commands.hgetall(keyPrefix + chatId + ":state");
        map.forEach(state::put);
        return state;
    }

    @Override
    protected void setChatState(long chatId, Map<String, String> state) {
        commands.del(keyPrefix + chatId + ":state");
        if (state != null && !state.isEmpty()) commands.hset(keyPrefix + chatId + ":state", state);
    }

    @Override
    protected Integer getMessageId(long chatId) {
        String messageId = commands.get(keyPrefix + chatId + ":messageId");
        if (messageId == null) return null;
        return Integer.parseInt(messageId);
    }

    @Override
    protected void setMessageId(long chatId, Integer messageId) {
        if (messageId == null)
            commands.del(keyPrefix + chatId + ":messageId");
        else
            commands.set(keyPrefix + chatId + ":messageId", messageId.toString());
    }
}
