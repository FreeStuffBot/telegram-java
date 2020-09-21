package com.github.rami_sabbagh.telegram.alice_framework.pipes;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A pipe which stores it's handlers using a collection.
 *
 * @param <T> The events type, ex: Update.
 */
public abstract class CollectionPipe<T> implements Pipe<T> {

    /**
     * The handlers collection.
     */
    protected final Collection<Handler<T>> handlers;

    /**
     * Create a CollectionPipe using an ArrayList as the container.
     */
    public CollectionPipe() {
        handlers = new ArrayList<>();
    }

    /**
     * Create a CollectionPipe using a specific container.
     *
     * @param handlers The container to store the handlers in.
     */
    public CollectionPipe(Collection<Handler<T>> handlers) {
        this.handlers = handlers;
    }

    /**
     * Gets an array of all the handlers currently registered in the pipe.
     *
     * @return The currently registered handlers in the pipe.
     */
    public Handler<T>[] getHandlers() {
        //noinspection unchecked
        return handlers.toArray(new Handler[0]);
    }

    @Override
    public boolean registerHandler(Handler<T> handler) {
        return handlers.add(handler);
    }

    @Override
    public boolean unregisterHandler(Handler<T> handler) {
        return handlers.remove(handler);
    }
}
