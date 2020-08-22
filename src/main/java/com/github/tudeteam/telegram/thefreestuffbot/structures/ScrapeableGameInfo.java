package com.github.tudeteam.telegram.thefreestuffbot.structures;

import java.net.URI;

/**
 * The data that can be found by the web scrapers.
 */
public class ScrapeableGameInfo {

    /**
     * Game's title.
     */
    public String title;

    /**
     * Original price before the discount.
     */
    public Price org_price;

    /**
     * Price after the discount.
     */
    public Price price;

    /**
     * Url to the thumbnail image.
     */
    public URI thumbnail;

    /**
     * UNIX Timestamp in seconds - marks the time when the offer expires.
     */
    public int until;

    /**
     * Nullable (not in GameInfo). For steam games, subids with a space between, for other stores just an empty string.
     */
    public String steamSubids;

    public static class Price {
        public double euro;
        public double dollar;
    }
}
