package com.github.tudeteam.telegram.thefreestuffbot.framework.interactives;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;
import java.util.function.BiFunction;

public class InteractiveBuilder {
    protected String id;
    protected BiFunction<Message, Map<String, String>, Message> process;

    /**
     * Sets the id of the interactive handler.
     *
     * @param id The id of the interactive handler for representation in the database.
     * @return this.
     */
    public InteractiveBuilder id(String id) {
        if (id == null) throw new NullPointerException("id can't be null!");
        this.id = id;
        return this;
    }

    /**
     * Sets the process method of the interactive handler.
     *
     * @param process The messages processor.
     * @return this.
     */
    public InteractiveBuilder process(BiFunction<Message, Map<String, String>, Message> process) {
        if (process == null) throw new NullPointerException("process can't be null!");
        this.process = process;
        return this;
    }

    /**
     * Constructs the Interactive with the current state of the builder.
     *
     * @return The interactive constructed.
     */
    public Interactive build() {
        if (id == null) throw new NullPointerException("The id was not set!");
        if (process == null) throw new NullPointerException("The process was not set!");
        return new ConstructedInteractive(id, process);
    }

    /**
     * An interactive implementation for using a {@code BiFunction} for processing the messages.
     */
    private static class ConstructedInteractive extends Interactive {
        /**
         * The process of the interactive.
         */
        protected final BiFunction<Message, Map<String, String>, Message> process;

        private ConstructedInteractive(String id, BiFunction<Message, Map<String, String>, Message> process) {
            super(id);
            this.process = process;
        }

        @Override
        public Message process(Message message, Map<String, String> state) {
            return process.apply(message, state);
        }
    }
}
