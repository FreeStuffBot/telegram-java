package com.github.tudeteam.telegram.thefreestuffbot.framework.pipes;

import java.util.function.Predicate;

/**
 * An events handler, which would process the events passed to it, and it if it consumed them.
 *
 * @param <T> The event type, ex: {@code Update}.
 */
public interface Handler<T> extends Predicate<T> {
    /**
     * Process an event.
     *
     * @param event the event to process.
     * @return {@code true} if event was consumed,
     * otherwise {@code false}
     */
    boolean process(T event);

    /**
     * Alias for {@link #process process()} so the handler can be used as a predicate.
     *
     * @param event the event to process.
     * @return {@code true} if event was consumed,
     * otherwise {@code false}
     */
    @Override
    default boolean test(T event) {
        return process(event);
    }
}
