package com.github.tudeteam.telegram.thefreestuffbot.database;

import java.util.HashMap;

/**
 * This is the object that gets stored long term for the following uses:
 * - Tell the proxy where to redirect the links to (redundant as the proxy is not in use yet).
 * - Contain analytics data.
 * - Queue up for approval process.
 */
public class GameData {
    /**
     * A unique number to identify the game - used by the proxy.
     */
    public int _id;

    /**
     * Internal uuid - used for checking if a game was already announced.
     */
    public String uuid;

    /**
     * UNIX Timestamp in seconds - marks the last time the approval status has changed.
     */
    public long published;

    /**
     * Discord user id of the moderator, responsible for checking the info and publishing the announcement.
     */
    public String responsible;

    /**
     * Current status of the game.
     */
    public GameApprovalStatus status;

    /**
     * Analytics data for the game.
     */
    public Analytics analytics;

    /**
     * Info about the game.
     */
    public GameInfo info;

    /**
     * Nullable. Array that gradually fills up with all the shards that have picked up the announcement.
     * Last shard that would make the list full will remove the object from the entry
     */
    public int[] outgoing;

    public static class Analytics {
        /**
         * Number of servers it got announced in.
         */
        public int reach;

        /**
         * Clicks in total.
         */
        public int clicks;

        /**
         * Clicks per guild.
         */
        public HashMap<String, Integer> guilds;
    }
}
