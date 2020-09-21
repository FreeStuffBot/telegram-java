package com.github.rami_sabbagh.telegram.alice_framework.pipes;

/**
 * An events handler, which would process the events passed to it, and it if it consumed them.
 * <p>
 * The handler is accepted as a functional interface and so it can be used with the lambda expression.
 *
 * @param <T> The event type, ex: {@code Update}.
 */
public interface Handler<T> {
    /**
     * Process an event.
     *
     * @param event the event to process.
     * @return {@code true} if event was consumed, otherwise {@code false}
     */
    boolean process(T event);
}
