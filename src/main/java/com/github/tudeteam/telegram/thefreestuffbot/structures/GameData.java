package com.github.tudeteam.telegram.thefreestuffbot.structures;

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
    public int published;

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
     * The outgoing progress of the announcement for different platforms.
     */
    public Outgoing outgoing;

    public static class Analytics {

        /**
         * The analytics of the Telegram bot.
         */
        public TelegramAnalytics telegram;
    }

    /**
     * Only Telegram is included here for simplicity reasons.
     */
    public static class Outgoing {

        /**
         * Nullable. The outgoing data of the Telegram announcement.
         */
        public TelegramOutgoing telegram;
    }
}
