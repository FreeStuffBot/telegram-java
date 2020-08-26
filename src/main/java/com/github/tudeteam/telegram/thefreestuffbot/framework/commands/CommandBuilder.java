package com.github.tudeteam.telegram.thefreestuffbot.framework.commands;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

/**
 * A builder class for commands, for creating commands with a sweeter syntax.
 */
public class CommandBuilder {
    protected String name;
    protected String description;
    protected Locality locality = Locality.ALL;
    protected Privacy privacy = Privacy.PUBLIC;
    protected BiConsumer<Message, ParsedCommand> action;

    /**
     * Sets the name of the command, <b>required</b>.
     *
     * @param name The name of the command, can't be null.
     * @return this.
     */
    public CommandBuilder name(String name) {
        if (name == null) throw new NullPointerException("Name can't be null!");
        this.name = name;
        return this;
    }

    /**
     * Sets the description of the command (optional).
     *
     * @param description The description of the command, can be null (optional).
     * @return this.
     */
    public CommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the locality of the command ({@code ALL} by default).
     *
     * @param locality The locality of the command, can't be null.
     * @return this.
     */
    public CommandBuilder locality(Locality locality) {
        if (locality == null) throw new NullPointerException("Locality can't be null!");
        this.locality = locality;
        return this;
    }

    /**
     * Sets the privacy of the command ({@code PUBLIC} by default).
     *
     * @param privacy The privacy of the command, can't be null.
     * @return this.
     */
    public CommandBuilder privacy(Privacy privacy) {
        if (privacy == null) throw new NullPointerException("Privacy can't be null!");
        this.privacy = privacy;
        return this;
    }

    /**
     * Sets the action of the command, <b>required</b>.
     *
     * @param action A consumer to be used when the command is executed.
     * @return this.
     */
    public CommandBuilder action(BiConsumer<Message, ParsedCommand> action) {
        if (action == null) throw new NullPointerException("Action can't be a null!");
        this.action = action;
        return this;
    }

    /**
     * Constructs the command with the current state of the builder.
     *
     * @return The command constructed.
     */
    public Command build() {
        if (name == null) throw new NullPointerException("Command's name has not been set!");
        if (action == null) throw new NullPointerException("Command's action has not been set!");
        return new ConstructedCommand(name, description, locality, privacy, action);
    }

    /**
     * A command implementation for using a {@code Consumer} for executing the command.
     */
    private static class ConstructedCommand extends Command {
        /**
         * The action of the command.
         */
        protected final BiConsumer<Message, ParsedCommand> action;

        private ConstructedCommand(String name, String description, Locality locality, Privacy privacy, BiConsumer<Message, ParsedCommand> action) {
            super(name, description, locality, privacy);
            this.action = action;
        }

        @Override
        public void action(Message message, ParsedCommand parsedCommand) {
            action.accept(message, parsedCommand);
        }
    }
}
