package com.github.tudeteam.telegram.thefreestuffbot.framework.pipes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilteredTest {

    @Test
    @DisplayName("Works with a single predicate")
    void testSinglePredicate() {
        //An always consuming handler.
        Handler<Boolean> handler = event -> true;

        //The predicate used for filtering.
        Predicate<Boolean> predicate = aBoolean -> aBoolean;

        //The filtered handler.
        Handler<Boolean> filtered = new Filtered<>(handler, predicate);

        //Test the filtered handler.
        assertTrue(filtered.process(true), "Should get consumed!");
        assertFalse(filtered.process(false), "Should not get consumed!");
    }

    @Test
    @DisplayName("Works with multiple predicates")
    void testMultiplePredicates() {
        //An always consuming handler.
        Handler<Integer> handler = event -> true;

        //The predicates used for filtering.
        Predicate<Integer> acceptOdd = integer -> integer % 2 == 1;
        Predicate<Integer> acceptEven = integer -> integer % 2 == 0;

        //The filtered handler.
        Handler<Integer> filtered = new Filtered<>(handler, acceptOdd, acceptEven);

        //Test the filtered handler.
        assertTrue(filtered.process(9), "Didn't consume a positive odd number!");
        assertTrue(filtered.process(16), "Didn't consume a positive even number!");
        assertFalse(filtered.process(-7), "Consumed a negative odd number!");
        assertTrue(filtered.process(-14), "Didn't consume a negative even number!");
    }
}