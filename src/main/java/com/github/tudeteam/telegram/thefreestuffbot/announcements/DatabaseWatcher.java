package com.github.tudeteam.telegram.thefreestuffbot.announcements;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Updates.set;

/**
 * Watches the MongoDB database for new published games announcements.
 * <p>
 * Should be scheduled to run at a fixed rate (like every minute).
 */
public class DatabaseWatcher implements Runnable {

    protected final Announcements announcements;
    protected final MongoCollection<Document> configCollection;
    protected final MongoCollection<Document> gamesCollection;
    protected final MongoCollection<Document> ongoingCollection;

    public DatabaseWatcher(Announcements announcements, MongoCollection<Document> configCollection, MongoCollection<Document> gamesCollection, MongoCollection<Document> ongoingCollection) {
        this.announcements = announcements;
        this.configCollection = configCollection;
        this.gamesCollection = gamesCollection;
        this.ongoingCollection = ongoingCollection;
    }

    @Override
    public synchronized void run() {
        gamesCollection.find(and(
                eq("status", "published"),
                ne("telegram", true)
        )).sort(ascending("published")).forEach(gameDocument -> {
            Document announcement = new Document();

            announcement.put("startedAt", (int) (System.currentTimeMillis() / 1000L));
            announcement.put("finishedAt", null);
            announcement.put("type", "GAME");
            announcement.put("data", new Document("_id", gameDocument.get("_id")));

            List<Long> pendingChats = new ArrayList<>();

            configCollection.find(eq("enabled", true)).forEach(config ->
                    pendingChats.add(config.getLong("_id")));

            announcement.put("chats", new Document("pending", pendingChats)
                    .append("processed", new ArrayList<>())
                    .append("failed", new ArrayList<>()));

            announcement.put("reached", new Document("users", 0)
                    .append("groups", 0)
                    .append("supergroups", 0)
                    .append("channels", 0));

            ongoingCollection.insertOne(announcement);
            gamesCollection.updateOne(gameDocument, set("telegram", true));
            announcements.wakeUp();
        });
    }
}
