package com.github.tudeteam.telegram.thefreestuffbot.components.announcements;

import com.github.rami_sabbagh.telegram.alice_framework.pipes.Handler;
import com.github.tudeteam.telegram.thefreestuffbot.TheFreeStuffBot;
import com.mongodb.client.MongoCollection;
import io.lettuce.core.api.sync.RedisCommands;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class AnnouncementsMigrationHandler implements Handler<Update> {

    protected final MongoCollection<Document> gamesCollection;
    protected final RedisCommands<String, String> commands;

    public AnnouncementsMigrationHandler(TheFreeStuffBot bot) {
        gamesCollection = bot.gamesCollection;
        commands = bot.redisCommands;
    }

    @Override
    public boolean process(Update update) {
        if (!update.hasMessage()) return false;
        Message message = update.getMessage();
        if (message.getMigrateFromChatId() == null || message.getMigrateToChatId() == null) return false;

        long fromChatId = message.getMigrateFromChatId();
        long toChatId = message.getMigrateToChatId();

        gamesCollection.find(and(
                eq("status", "published"), //TODO: Change into 'accepted'.
                eq("outgoing.telegram", true)
        )).forEach(document -> {
            String keyPrefix = "TheFreeStuffBot:ongoing:" + document.getInteger("_id") + ":";
            String keyActive = keyPrefix + "active";
            String keyPending = keyPrefix + "pending";
            String keyFailed = keyPrefix + "failed";

            if (commands.get(keyActive) == null) return;

            long pendingCount = commands.lrem(keyPending, 0, String.valueOf(fromChatId));
            long failedCount = commands.lrem(keyFailed, 0, String.valueOf(fromChatId));

            for (int i = 0; i < pendingCount; i++) commands.rpush(keyPending, String.valueOf(toChatId));
            for (int i = 0; i < failedCount; i++) commands.rpush(keyFailed, String.valueOf(toChatId));
        });

        return false; //The migration messages should not be consumed, so they get passed to all the bot's components.
    }
}
