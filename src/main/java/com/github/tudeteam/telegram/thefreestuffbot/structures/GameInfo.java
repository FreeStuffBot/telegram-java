package com.github.tudeteam.telegram.thefreestuffbot.structures;

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
    public int flags;

    /**
     * Type of announcement.
     */
    public AnnouncementType type;

    /**
     * Checks whether the game has the specified flag or not.
     *
     * @param flag The flag to check.
     * @return {@code true} if it's set, {@code false} otherwise.
     */
    public boolean hasFlag(GameFlag flag) {
        return flag.isSet(flags);
    }

    public String formatCaption(ChatConfiguration config) {
        return String.format("<b>Free Game!</b>\n<b>%s</b>\n<s>%s</s> <b>Free</b> until %s • %s\nvia freestuffbot.xyz",
                title,
                org_price.toString(config.currency),
                formatUntil(config.untilFormat),
                store.toString()
        );
    }
}
