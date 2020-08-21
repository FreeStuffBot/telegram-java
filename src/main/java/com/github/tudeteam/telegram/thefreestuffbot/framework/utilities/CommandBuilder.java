package com.github.tudeteam.telegram.thefreestuffbot.framework.utilities;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Command;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Locality;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.ParsedCommand;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Privacy;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.function.BiConsumer;

public class CommandBuilder {
    protected String name;
    protected String description;
    protected Locality locality = Locality.ALL;
    protected Privacy privacy = Privacy.PUBLIC;
    protected BiConsumer<Message, ParsedCommand> action;

    public CommandBuilder name(String name) {
        if (name == null) throw new NullPointerException("Name can't be null!");
        this.name = name;
        return this;
    }

    public CommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    public CommandBuilder locality(Locality locality) {
        if (locality == null) throw new NullPointerException("Locality can't be null!");
        this.locality = locality;
        return this;
    }

    public CommandBuilder privacy(Privacy privacy) {
        if (privacy == null) throw new NullPointerException("Privacy can't be null!");
        this.privacy = privacy;
        return this;
    }

    public CommandBuilder action(BiConsumer<Message, ParsedCommand> action) {
        if (action == null) throw new NullPointerException("Action can't be a null!");
        this.action = action;
        return this;
    }

    public Command build() {
        if (name == null) throw new NullPointerException("Command's name has not been set!");
        if (action == null) throw new NullPointerException("Command's action has not been set!");
        return new ConstructedCommand(name, description, locality, privacy, action);
    }

    private static class ConstructedCommand extends Command {
        protected final BiConsumer<Message, ParsedCommand> action;

        private ConstructedCommand(String name, String description, Locality locality, Privacy privacy, BiConsumer<Message, ParsedCommand> action) {
            super(name, description, locality, privacy);
            this.action = action;
        }

        @Override
        public void action(Message source, ParsedCommand parsedCommand) {
            action.accept(source, parsedCommand);
        }
    }
}
