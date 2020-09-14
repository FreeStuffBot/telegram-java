package com.github.tudeteam.telegram.thefreestuffbot.structures;

public class TelegramOutgoing {

    /**
     * Always set to true, indicates that the Telegram bot has noticed the announcement and processing it.
     */
    public boolean active;

    /**
     * The ids of pending chats.
     */
    public long[] pending;

    /**
     * The ids of chats which failed, to be requeued (to pending) once the current pending is finished.
     */
    public long[] failed;

    /**
     * The number of retry attempts remaining for the failed chats.
     */
    public int attempts;
}
