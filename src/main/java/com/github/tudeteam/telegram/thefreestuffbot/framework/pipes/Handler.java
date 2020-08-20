package com.github.tudeteam.telegram.thefreestuffbot.framework.pipes;

/**
 * An events handler, which would process the events passed to it, and it if it consumed them.
 *
 * @param <T> The event type, ex: {@code Update}.
 */
public interface Handler<T> {
    /**
     * Process an event.
     *
     * @param event the event to process.
     * @return {@code true} if event was consumed,
     * otherwise {@code false}
     */
    boolean process(T event);
}
