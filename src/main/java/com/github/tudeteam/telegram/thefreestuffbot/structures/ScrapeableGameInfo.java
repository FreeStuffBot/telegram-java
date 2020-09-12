package com.github.tudeteam.telegram.thefreestuffbot.structures;

import java.net.URI;
import java.time.LocalDate;

import static java.time.format.TextStyle.FULL_STANDALONE;
import static java.util.Locale.ENGLISH;

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

    /**
     * Formats the until date of the game.
     *
     * @param untilFormat The format to use.
     * @return The until date formatted into a String.
     */
    public String formatUntil(UntilFormat untilFormat) {
        LocalDate date = LocalDate.ofEpochDay(until / 86400); //86400 is the number of seconds in a day.
        if (untilFormat == UntilFormat.DATE)
            return date.toString();
        else
            return "next " + date.getDayOfWeek().getDisplayName(FULL_STANDALONE, ENGLISH);
    }

    public static class Price {
        public double euro;
        public double dollar;

        /**
         * Gets the price of the game in the requested currency.
         *
         * @param currency The currency to use.
         * @return The price of the game in the requested currency.
         */
        public double inCurrency(Currency currency) {
            return currency == Currency.USD ? dollar : euro;
        }

        /**
         * Converts the price into a string with a specific currency.
         *
         * @param currency The currency to use.
         * @return The formatted string.
         */
        public String toString(Currency currency) {
            if (currency == Currency.USD)
                return "$" + dollar;
            else
                return "€" + euro;
        }
    }
}
