package com.github.tudeteam.telegram.thefreestuffbot.structures;

public class ChatConfiguration {

    public static final ChatConfiguration defaultConfig = new ChatConfiguration();

    static {
        defaultConfig._id = 0;
        defaultConfig.enabled = true;
        defaultConfig.currency = Currency.USD;
        defaultConfig.untilFormat = UntilFormat.DATE;
        defaultConfig.trash = false;
        defaultConfig.minPrice = 0.0;
    }

    /**
     * The chatId to send the announcements to.
     */
    public long _id;

    /**
     * Whether the announcements are enabled in this chat or not.
     */
    public boolean enabled;

    /**
     * The currency to use when sending the announcements.
     */
    public Currency currency;

    /**
     * The format to display the until date in.
     */
    public UntilFormat untilFormat;

    /**
     * Whether to announce trash games (when {@code true}), or to filter them ({@code false}, default).
     */
    public boolean trash;

    /**
     * The minimum original price of the game inorder to announce it.
     */
    public double minPrice;
}
