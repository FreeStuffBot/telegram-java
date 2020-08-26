package com.github.tudeteam.telegram.thefreestuffbot.framework.pipes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ConsumeOncePipeTest extends PipeTest {

    ConsumeOncePipe<Object> consumeOncePipe = new ConsumeOncePipe<>();
    ConsumeOncePipe<Object> consumeOncePipeSet = new ConsumeOncePipe<>(new HashSet<>());

    @Test
    @DisplayName("Consumes events only once")
    void testConsumeOncePipe() {
        assertFalse(consumeOncePipe.process(new Object()), "the pipe consumed the event with no handlers");
        assertTrue(consumeOncePipe.registerHandler((event -> false)), "failed to register test handler");
        assertFalse(consumeOncePipe.process(new Object()), "the pipe consumed the event with a non consumer handler");
        assertTrue(consumeOncePipe.registerHandler((event -> true)), "failed to register test handler");
        assertTrue(consumeOncePipe.process(new Object()), "the pipe didn't consume the event with an always consumer handler");
        assertTrue(consumeOncePipe.registerHandler((event -> {
            throw new AssertionError("the pipe passed the event even after being consumed");
        })), "failed to register test handler");
        assertTrue(consumeOncePipe.process(new Object()), "the pipe didn't consume the event with an always consumer handler");
        consumeOncePipe.handlers.clear();
    }

    @Test
    @DisplayName("Can be used with a HashSet")
    void testPipeUsingSet() {
        Handler<Object> h1 = (event -> false);
        Handler<Object> h2 = (event -> false);

        assertTrue(consumeOncePipeSet.registerHandler(h1), "failed to register h1!");
        assertTrue(consumeOncePipeSet.registerHandler(h2), "failed to register h2!");
        assertFalse(consumeOncePipeSet.registerHandler(h1), "h1 got registered twice in a set pipe!");

        assertTrue(consumeOncePipeSet.handlers.contains(h1), "h1 was not found in the handlers collection!");
        assertTrue(consumeOncePipeSet.handlers.contains(h2), "h2 was not found in the handlers collection!");

        assertTrue(consumeOncePipeSet.unregisterHandler(h1), "failed to unregister h1!");
        assertTrue(consumeOncePipeSet.unregisterHandler(h2), "failed to unregister h2!");
        assertFalse(consumeOncePipeSet.unregisterHandler(h1), "h1 got unregistered twice in a set pipe!");
    }

    @Test
    @DisplayName("Provides a list of it's handlers")
    void testGetHandlers() {
        for (int i = 0; i < 5; i++)
            assertTrue(consumeOncePipe.registerHandler((event) -> false), "Failed to register handler #" + i);

        Handler<Object>[] handlers = consumeOncePipe.getHandlers();
        assertEquals(handlers.length, 5, "Handlers count mismatched!");

        for (int i = 0; i < handlers.length; i++)
            assertTrue(consumeOncePipe.unregisterHandler(handlers[i]), "Failed to unregister handler #" + i);
    }

    @Test
    @DisplayName("Registers handlers")
    void executeHandlerRegistrationTest() {
        testHandlerRegistration(consumeOncePipe);
        testHandlerRegistration(consumeOncePipeSet);
    }

    @Test
    @DisplayName("Unregisters handlers")
    void executeNullTriggerTest() {
        testNullTrigger(consumeOncePipe);
        testNullTrigger(consumeOncePipeSet);
    }
}