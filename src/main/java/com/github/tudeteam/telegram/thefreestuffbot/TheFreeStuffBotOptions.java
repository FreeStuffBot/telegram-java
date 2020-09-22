package com.github.tudeteam.telegram.thefreestuffbot;

import com.github.rami_sabbagh.telegram.alice_framework.bots.alice.AliceOptions;

public class TheFreeStuffBotOptions extends AliceOptions {
    @Override
    public String botToken() {
        return System.getenv("BOT_TOKEN");
    }

    @Override
    public String botUsername() {
        return System.getenv("BOT_USERNAME");
    }

    @Override
    public int botCreatorId() {
        return Integer.parseInt(System.getenv("BOT_CREATORID"));
    }

    @Override
    public String mongoConnectionURI() {
        return System.getenv("BOT_DATABASE");
    }

    @Override
    public String redisConnectionURI() {
        return "redis://localhost:6379";
    }

    @Override
    public int threadsCount() {
        return 6;
    }

    @Override
    public String mongoDatabaseName() {
        return "freestuffbot";
    }

    @Override
    public String mongoCollectionName(Collection collection) {
        switch (collection) {
            case ADMINS:
                return "telegram-admins";
            case CHATS:
                return "telegram-chats";
            default:
                return super.mongoCollectionName(collection);
        }
    }
}
