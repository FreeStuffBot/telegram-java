package com.github.rami_sabbagh.telegram.alice_framework.redis.interactivity;

import com.github.rami_sabbagh.telegram.alice_framework.interactivity.InteractivityHandler;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.HashMap;
import java.util.Map;

public class RedisInteractivityHandler extends InteractivityHandler {

    protected final String keyPrefix;
    protected final RedisCommands<String, String> commands;

    /**
     * Creates a new InteractivityHandler which stores it's data on a redis database.
     *
     * @param namespace The namespace to prefix the redis keys with.
     * @param commands  The redis sync commands to use.
     */
    public RedisInteractivityHandler(String namespace, RedisCommands<String, String> commands) {
        keyPrefix = namespace + ":interactivity:";
        this.commands = commands;
    }

    @Override
    protected void setActiveListenerId(long chatId, String listenerId) {
        commands.set(keyPrefix + chatId + "listenerId", listenerId);
    }

    @Override
    protected String getActiveListenerId(long chatId) {
        return commands.get(keyPrefix + chatId + ":listenerId");
    }

    @Override
    protected void setMessageId(long chatId, Integer messageId) {
        if (messageId == null)
            commands.del(keyPrefix + chatId + ":messageId");
        else
            commands.set(keyPrefix + chatId + ":messageId", messageId.toString());
    }

    @Override
    protected Integer getMessageId(long chatId) {
        String value = commands.get(keyPrefix + chatId + ":messageId");
        if (value == null) return null;
        return Integer.parseInt(value);
    }

    @Override
    protected void setStateData(long chatId, Map<String, String> data) {
        commands.del(keyPrefix + chatId + ":data");
        if (data != null && !data.isEmpty()) commands.hset(keyPrefix + chatId + ":data", data);
    }

    @Override
    protected Map<String, String> getStateData(long chatId) {
        Map<String, String> data = commands.hgetall(keyPrefix + chatId + ":data");
        return new HashMap<>(data); //Clone it so it's a modifiable HashMap for sure.
    }
}
