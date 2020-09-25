package com.github.tudeteam.telegram.thefreestuffbot.components;

import com.github.tudeteam.telegram.thefreestuffbot.TheFreeStuffBot;
import com.github.tudeteam.telegram.thefreestuffbot.structures.ChatConfiguration;
import com.github.tudeteam.telegram.thefreestuffbot.structures.Currency;
import com.github.tudeteam.telegram.thefreestuffbot.structures.UntilFormat;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class ConfigurationDB {

    protected static final Gson gson = new Gson();

    protected final MongoCollection<Document> collection;

    public ConfigurationDB(TheFreeStuffBot bot) {
        this.collection = bot.configCollection;
    }

    /**
     * Checks whether the configuration for a chat exists or not.
     *
     * @param chatId The chat id.
     * @return {@code true} if it exists.
     */
    public boolean exists(long chatId) {
        return collection.find(eq("_id", chatId)).first() != null;
    }

    /**
     * Creates new configuration entry for a chat.
     *
     * @param chatId The chat id.
     * @return {@code true} on success.
     */
    public boolean newConfiguration(long chatId) {
        return collection.insertOne(new Document("_id", chatId)
                .append("enabled", ChatConfiguration.defaultConfig.enabled)
                .append("currency", ChatConfiguration.defaultConfig.currency.name())
                .append("untilFormat", ChatConfiguration.defaultConfig.untilFormat.name())
                .append("trash", ChatConfiguration.defaultConfig.trash)
                .append("minPrice", ChatConfiguration.defaultConfig.minPrice)).wasAcknowledged();
    }

    /**
     * Deletes the configuration of a chat.
     *
     * @param chatId The chat id.
     * @return {@code true} on success.
     */
    public boolean deleteConfiguration(long chatId) {
        return collection.deleteOne(eq("_id", chatId)).getDeletedCount() == 1;
    }

    /**
     * Gets the current configuration of a chat.
     *
     * @param chatId The chat id.
     * @return The configuration of the chat, {@code null} when not found.
     */
    public ChatConfiguration getConfiguration(long chatId) {
        Document document = collection.find(eq("_id", chatId)).first();
        if (document == null) return null;

        return gson.fromJson(document.toJson(), ChatConfiguration.class);
    }

    public boolean setAnnouncements(long chatId, boolean enabled) {
        return collection.updateOne(eq("_id", chatId), set("enabled", enabled)).getMatchedCount() == 1;
    }

    public boolean setCurrency(long chatId, Currency currency) {
        return collection.updateOne(eq("_id", chatId), set("currency", currency.name())).getMatchedCount() == 1;
    }

    public boolean setUntilFormat(long chatId, UntilFormat untilFormat) {
        return collection.updateOne(eq("_id", chatId), set("untilFormat", untilFormat.name())).getMatchedCount() == 1;
    }

    public boolean setTrash(long chatId, boolean trash) {
        return collection.updateOne(eq("_id", chatId), set("trash", trash)).getMatchedCount() == 1;
    }

    public boolean setMinPrice(long chatId, double minPrice) {
        return collection.updateOne(eq("_id", chatId), set("minPrice", minPrice)).getMatchedCount() == 1;
    }

    public boolean isAnnouncementsEnabled(long chatId) {
        Document document = collection.find(eq("_id", chatId)).first();
        if (document == null) return false;
        return document.getBoolean("enabled");
    }
}
