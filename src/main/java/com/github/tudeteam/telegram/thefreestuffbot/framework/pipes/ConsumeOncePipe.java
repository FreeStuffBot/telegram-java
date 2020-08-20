package com.github.tudeteam.telegram.thefreestuffbot.framework.pipes;

import java.util.Collection;

/**
 * A pipe which consumes an event only once.
 * <p>
 * This works by passing the event to each handler, until one of them consumes it.
 * Once that happens the processing finishes and the pipe returns.
 *
 * @param <T> The events type, ex: Update.
 */
public class ConsumeOncePipe<T> extends CollectionPipe<T> {
    /**
     * Create a ConsumePipe using an ArrayList as the container.
     * Which allows the handlers to be stored in order, and get duplicated.
     */
    public ConsumeOncePipe() {
        super();
    }

    /**
     * Create a ConsumeOncePipe using a specific container.
     *
     * @param handlers The container to store the handlers in.
     */
    public ConsumeOncePipe(Collection<Handler<T>> handlers) {
        super(handlers);
    }

    /**
     * Process an event by the pipe, allowing it to be consumed only once.
     * <p>
     * This works by passing the event to each handler, until one of them consumes it.
     * Once that happens the processing finishes and the method returns.
     *
     * @param event the event to process.
     * @return {@code true} if event was consumed by the pipe,
     * otherwise {@code false}
     */
    @Override
    public boolean process(T event) {
        for (Handler<T> handler : handlers) {
            if (handler.process(event))
                return true;
        }
        return false;
    }
}
