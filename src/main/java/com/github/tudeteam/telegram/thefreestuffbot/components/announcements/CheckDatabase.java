package com.github.tudeteam.telegram.thefreestuffbot.components.announcements;

import com.github.rami_sabbagh.telegram.alice_framework.utilities.SilentExecutor;
import com.github.tudeteam.telegram.thefreestuffbot.TheFreeStuffBot;
import com.github.tudeteam.telegram.thefreestuffbot.components.ConfigurationDB;
import com.github.tudeteam.telegram.thefreestuffbot.structures.GameInfo;
import com.github.tudeteam.telegram.thefreestuffbot.structures.TelegramAnalytics;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import io.lettuce.core.api.sync.RedisCommands;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Updates.*;

/**
 * Checks the database for new game announcements, and processes them, then terminates.
 * <p>
 * The redis structure for announcements is:
 * <p>
 * Keys format: TheFreeStuffBot:ongoing:{game_id}:{field_id}
 * <p>
 * Fields:
 *
 * <ul>
 *     <li><b>active:</b> A string "true" indicating that the announcement has been initialized.</li>
 *     <li><b>pending</b> A set of chat ids to announce to.</li>
 *     <li><b>failed:</b> A set of chat ids which failed, to be requeued (to pending) once the current pending is finished.</li>\
 *     <li><b>attempts:</b> The number of retry attempts remaining.</li>
 *
 *     <li><b>users:</b>A counter of announcements sent to users.</li>
 *     <li><b>groups:</b>A counter of announcements sent to groups.</li>
 *     <li><b>supergroups:</b>A counter of announcements sent to supergroups.</li>
 *     <li><b>channels:</b>A counter of announcements sent to channels.</li>
 *     <li><b>groupsUsers:</b>A counter of the number of users in groups which the announcement was sent to.</li>
 *     <li><b>channelsUsers:</b>A counter of the number of users in channels which the announcement was sent to.</li>
 * </ul>
 */
public class CheckDatabase implements Runnable {

    /* Static Fields */

    /**
     * The number of retry attempts for failed announcements.
     */
    protected final static int retryAttempts = 3;
    protected final static int workersCount = 5;
    protected final Gson gson = new Gson();

    /* Instance Fields */

    protected final RateLimitBucket rateLimitBucket = new RateLimitBucket(20); //20 announcements per second.

    //Blank finals.
    protected final SilentExecutor silent;
    protected final ExecutorService executor;
    protected final MongoCollection<Document> configCollection;
    protected final MongoCollection<Document> gamesCollection;
    protected final RedisCommands<String, String> redisCommands;
    protected final ConfigurationDB db;

    /* Constructor */

    public CheckDatabase(TheFreeStuffBot bot) {
        silent = bot.silent;
        executor = bot.executor;
        configCollection = bot.configCollection;
        gamesCollection = bot.gamesCollection;
        redisCommands = bot.redisCommands;
        db = bot.configurationDB;
    }

    /* Instance Methods */

    /**
     * Initializes the announcement data structure on the redis database.
     * Unless if it was already initialized.
     *
     * @param gameDocument The MongoDB document of the game to announce.
     */
    protected void initializeAnnouncement(Document gameDocument) {
        //The prefix of all the redis keys for this game's announcement.
        String keyPrefix = "TheFreeStuffBot:ongoing:" + gameDocument.getInteger("_id") + ":";
        String keyActive = keyPrefix + "active";
        String keyPending = keyPrefix + "pending";
        String keyFailed = keyPrefix + "failed";
        String keyAttempts = keyPrefix + "attempts";
        String[] analyticsFields = {"users", "groups", "supergroups", "channels", "groupsUsers", "channelsUsers"};

        //Check if the announcement was not already initialized.
        if (redisCommands.exists(keyActive) == 0) {
            //Create the set of chat ids to announce to.
            //TODO: Use MongoDB's aggregation framework.
            configCollection.find(eq("enabled", true)).forEach(configDocument ->
                    redisCommands.sadd(keyPending, configDocument.getLong("_id").toString()));
            //Make sure to wipe the failed chats ids set (if it was leftover by the system somehow).
            redisCommands.del(keyFailed);
            //Set the attempts counter.
            redisCommands.set(keyAttempts, String.valueOf(retryAttempts));
            //Clear the analytics counters.
            for (String field : analyticsFields) redisCommands.set(keyPrefix + field, "0");
            //Set the announcement as initialized.
            redisCommands.set(keyActive, "true");
        }
    }

    protected void deleteAnnouncement(Document gameDocument) {
        String keyPrefix = "TheFreeStuffBot:ongoing:" + gameDocument.getInteger("_id") + ":";
        String[] fields = {"active", "pending", "failed", "attempts",
                "users", "groups", "supergroups", "channels", "groupsUsers", "channelsUsers"};

        for (int i = 0; i < fields.length; i++) fields[i] = keyPrefix + fields[i];

        System.out.println("Failed chats count: " + redisCommands.scard(fields[2]));
        redisCommands.del(fields);
    }

    @Override
    public void run() {
        try {
            //Search for a game announcement which is 'accepted' and set to be published on Telegram.
            gamesCollection.find(and(
                    eq("status", "published"), //TODO: Change into 'accepted'.
                    eq("outgoing.telegram", true)
            ))
                    .sort(ascending("published")) //Sort the results by the published time, for proper order.
                    .forEach(gameDocument -> { //Now for each game that meets the criteria, announce it on Telegram.
                        //The redis keys prefix for this game announcement.
                        String keyPrefix = "TheFreeStuffBot:ongoing:" + gameDocument.getInteger("_id") + ":";

                        //Deserialize the game's information.
                        GameInfo gameInfo = gson.fromJson(gameDocument.get("info", Document.class).toJson(), GameInfo.class);

                        //Make sure the announcement structure is initialized on the redis database.
                        initializeAnnouncement(gameDocument);

                        //Start the workers of the announcement.
                        List<Future<?>> futures = new ArrayList<>();
                        for (int i = 0; i < workersCount; i++)
                            futures.add(executor.submit(new AnnouncementWorker(gameDocument.getInteger("_id"),
                                    gameInfo, silent, db, rateLimitBucket, redisCommands)));

                        //Wait for all the workers to finish.
                        for (Future<?> future : futures) {
                            try {
                                future.get();
                            } catch (InterruptedException e) {
                                break;
                            } catch (ExecutionException e) {
                                System.err.println("An announcement worker has crashed!!!");
                                e.printStackTrace();
                                //TODO: Start a replacement worker.
                            }
                        }

                        //Check if the pending set is not empty (the announcement was not completed, and the workers died for some reason).
                        if (redisCommands.scard(keyPrefix + "pending") != 0)
                            return; //Prevent the game from being marked as announced. //TODO: Report the accident.

                        //Get the analytics data of the announcement.
                        TelegramAnalytics analytics = new TelegramAnalytics();

                        analytics.reach.users = Integer.parseInt(redisCommands.get(keyPrefix + "users"));
                        analytics.reach.groups = Integer.parseInt(redisCommands.get(keyPrefix + "groups"));
                        analytics.reach.supergroups = Integer.parseInt(redisCommands.get(keyPrefix + "supergroups"));
                        analytics.reach.channels = Integer.parseInt(redisCommands.get(keyPrefix + "channels"));
                        analytics.reach.groupsUsers = Integer.parseInt(redisCommands.get(keyPrefix + "groupsUsers"));
                        analytics.reach.channelsUsers = Integer.parseInt(redisCommands.get(keyPrefix + "channelsUsers"));

                        //Mark the game as announced for Telegram, and set the analytics data.
                        gamesCollection.updateOne(gameDocument, combine(
                                unset("outgoing.telegram"),
                                set("analytics.telegram", Document.parse(gson.toJson(analytics)))
                        ));

                        //Delete the announcement data from redis.
                        deleteAnnouncement(gameDocument);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
