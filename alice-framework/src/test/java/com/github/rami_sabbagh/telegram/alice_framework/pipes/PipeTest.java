package com.github.rami_sabbagh.telegram.alice_framework.pipes;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PipeTest {

    /**
     * Tests the handler registration and de-registration of the pipe.
     *
     * @param pipe The pipe to test.
     * @param <T>  The event type of the pipe.
     */
    <T> void testHandlerRegistration(Pipe<T> pipe) {
        Handler<T> handler = (event -> false);
        assertTrue(pipe.registerHandler(handler), "failed to register the handler!");
        assertTrue(pipe.unregisterHandler(handler), "failed to unregister the handler!");
    }

    /**
     * Tests if the pipe calls the handler with a null event for processing.
     *
     * @param pipe The pipe to test.
     * @param <T>  The event type of the pipe.
     */
    <T> void testNullTrigger(Pipe<T> pipe) {
        AtomicBoolean triggered = new AtomicBoolean(false);
        Handler<T> handler = (event -> {
            triggered.set(true);
            return true;
        });

        assertTrue(pipe.registerHandler(handler), "failed to register the handler!");

        pipe.process(null);

        assertTrue(pipe.unregisterHandler(handler), "failed to unregister the handler!");

        assertTrue(triggered.get(), "the handler was not triggered!");
    }
}