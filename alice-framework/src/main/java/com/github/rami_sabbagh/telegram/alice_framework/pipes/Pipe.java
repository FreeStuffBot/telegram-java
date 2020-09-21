package com.github.rami_sabbagh.telegram.alice_framework.pipes;

/**
 * A pipe is such a system which receives in <i>events</i> to process,
 * and indicate if it "consumed" the event.
 * <p>
 * An event being consumed means it was processed by the pipe, and there's no more need to pass
 * it into other systems.
 * <p>
 * Each event passed to the pipe is passed through "handlers" registered to it.
 * <p>
 * Every handler processes the event, and if it was consumed it would return true.
 * <p>
 * The pipe is implemented as a handler, so pipes can be nested within each other.
 *
 * @param <T> The events type, ex: Update.
 */
public interface Pipe<T> extends Handler<T> {

    /**
     * Process an event by the pipe.
     *
     * @param event the event to process.
     * @return {@code true} if event was consumed by the pipe,
     * otherwise {@code false}
     */
    boolean process(T event);

    /**
     * Registers an event handler in the pipe.
     *
     * @param handler The handler, which should return {@code true} when it consumes the event.
     * @return {@code true} if the handler has been registered successfully.
     */
    boolean registerHandler(Handler<T> handler);

    /**
     * Unregisters an event handler from the pipe.
     *
     * @param handler The handler to unregister.
     * @return {@code true} if the handler was registered previously.
     */
    boolean unregisterHandler(Handler<T> handler);
}
