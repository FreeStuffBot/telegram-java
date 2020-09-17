package com.github.tudeteam.telegram.thefreestuffbot.structures;

/**
 * This object contains analytics information for announcements made using the Telegram bot.
 */
public class TelegramAnalytics {

    /**
     * The numbers of chats the game was announced to.
     */
    public Reach reach = new Reach();

    /**
     * TODO: The total clicks count on the announcements (not-implemented).
     */
    public int clicks;

    /**
     * Multiple counters for different types of chats.
     */
    public static class Reach {

        /**
         * Number of users the game was announced to using private messages.
         */
        public int users;

        /**
         * Number of normal groups the game was announced to.
         */
        public int groups;

        /**
         * Number of super groups the game was announced to.
         */
        public int supergroups;

        /**
         * Number of channels the game was announced to.
         */
        public int channels;

        /**
         * The total number of users in groups which the game was announce to.
         */
        public int groupsUsers;

        /**
         * The total number of users subscribed to channels which the game was announced to.
         */
        public int channelsUsers;
    }
}
