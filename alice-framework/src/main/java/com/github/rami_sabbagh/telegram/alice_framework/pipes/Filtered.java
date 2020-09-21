package com.github.rami_sabbagh.telegram.alice_framework.pipes;

import java.util.function.Predicate;

/**
 * A filtered handler which would only process the event if the filter predicate accepted the event.
 *
 * @param <T> The event type, ex: Update.
 */
public class Filtered<T> implements Handler<T> {
    /**
     * The handler to process the accepted events using.
     */
    private final Handler<T> handler;

    /**
     * The events filter predicate.
     */
    private final Predicate<T> filter;

    /**
     * Constructs a filtered handler using the filter provided.
     *
     * @param handler The handler to process the accepted events using.
     * @param filter  The events filter predicate.
     */
    public Filtered(Handler<T> handler, Predicate<T> filter) {
        this.handler = handler;
        this.filter = filter;
    }

    /**
     * Constructs a filtered handler using multiple filter predicates.
     * It's enough for a single predicate to accept the event for it to be processed.
     *
     * @param handler The handler to process the accepted events using.
     * @param filters The event filter predicates.
     */
    @SafeVarargs
    public Filtered(Handler<T> handler, Predicate<T>... filters) {
        this.handler = handler;

        Predicate<T>[] predicates = filters.clone();

        this.filter = event -> {
            for (Predicate<T> filter : predicates)
                if (filter.test(event)) return true;
            return false;
        };
    }

    /**
     * Processes the event only if the filter predicate accepts it.
     *
     * @param event the event to process.
     * @return Whether the event was consumed or not.
     */
    @Override
    public boolean process(T event) {
        if (filter.test(event))
            return handler.process(event);
        return false;
    }
}
