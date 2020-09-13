package com.github.tudeteam.telegram.thefreestuffbot.announcements;

import com.github.tudeteam.telegram.thefreestuffbot.ConfigurationDB;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.concurrent.ExecutorService;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;

public class Announcements {

    protected static final int maxActiveWorkers = 4;
    protected final int creatorId;
    protected final ExecutorService executor;
    protected final SilentExecutor silent;
    protected final ConfigurationDB db;
    protected final MongoCollection<Document> ongoingCollection;
    protected final MongoCollection<Document> processedCollection;
    protected final MongoCollection<Document> gamesCollection;
    protected final Object lock = new Object();
    protected Boolean awake = false;
    protected ObjectId activeId = null;
    protected int activeWorkers;

    public Announcements(int creatorId, ExecutorService executor, SilentExecutor silent, ConfigurationDB db, MongoCollection<Document> ongoingCollection, MongoCollection<Document> processedCollection, MongoCollection<Document> gamesCollection) {
        this.creatorId = creatorId;
        this.executor = executor;
        this.silent = silent;
        this.db = db;
        this.ongoingCollection = ongoingCollection;
        this.processedCollection = processedCollection;
        this.gamesCollection = gamesCollection;
    }

    protected void nextAnnouncement() {
        try {
            Thread.sleep(1010L);
        } catch (InterruptedException ignored) {
        }

        Document latest = ongoingCollection.find().sort(ascending("startedAt")).first();
        if (latest == null) { //No more announcements to process, get back to sleep.
            awake = false;
            return;
        }

        activeId = latest.getObjectId("_id");
        activeWorkers = 0;

        activateWorkers(latest.getString("type"), latest.get("data", Document.class));
    }

    protected void activateWorkers(String announcementType, Document announcementData) {
        activeWorkers = maxActiveWorkers;
        for (int i = 0; i < maxActiveWorkers; i++) {
            executor.submit(new AnnouncementWorker(silent, db, ongoingCollection, gamesCollection, activeId, announcementType, announcementData) {
                @Override
                protected void finishedQueue() {
                    synchronized (lock) {
                        activeWorkers--;
                        if (activeWorkers == 0) {
                            Document document = ongoingCollection.findOneAndDelete(eq("_id", announcementId));
                            if (document != null) {
                                document.put("finishedAt", (int) (System.currentTimeMillis() / 1000L));
                                processedCollection.insertOne(document);
                            }

                            nextAnnouncement();
                        }
                    }
                }
            });
        }
    }

    /**
     * Wakes up the announcements workers if necessary.
     */
    public void wakeUp() {
        if (awake) return;

        awake = true;
        nextAnnouncement();
    }
}
