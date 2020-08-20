package com.github.tudeteam.telegram.thefreestuffbot.framework.pipes;

import java.util.ArrayList;
import java.util.List;

/**
 * A pipe which stores it's handlers within a List.
 *
 * @param <T> The events type, ex: Update.
 */
public abstract class ListPipe<T> implements Pipe<T> {
    /**
     * The handlers List, implemented as an ArrayList.
     */
    protected List<Handler<T>> handlers = new ArrayList<>();

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
    public void registerHandler(Handler<T> handler) {
        handlers.add(handler);
    }

    @Override
    public boolean unregisterHandler(Handler<T> handler) {
        return handlers.remove(handler);
    }
}
