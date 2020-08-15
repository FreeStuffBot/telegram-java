package com.github.tudeteam.telegram.thefreestuffbot.database;

import java.net.URI;

/**
 * An object with all the data needed to generate all types of announcements
 */
public class GameInfo extends ScrapeableGameInfo {
    /**
     * Proxy url.
     */
    public URI url;

    /**
     * The direct link to the store page
     */
    public URI org_url;

    /**
     * Game's store.
     */
    public Store store;

    /**
     * Flags.
     */
    public GameFlag[] flags;

    /**
     * Type of announcement.
     */
    public AnnouncementType type;
}
