package com.github.tudeteam.telegram.thefreestuffbot.framework.commands;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.Authorizer;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.DefaultAuthorizer;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.Handler;
import org.telegram.telegrambots.meta.api.objects.Update;

public class CommandsHandler implements Handler<Update> {

    /**
     * The predicate used to determine if the user is allowed to use the command or not.
     */
    public final Authorizer authorizer;

    /**
     * Contains the registered commands.
     */
    public final Map<String, Command> commands = new HashMap<>();

    //TODO: Create a base bot class for the framework bots so it's not polling bots only.
    public final TelegramLongPollingBot bot;

    /**
     * Creates a command handler with public commands authorization only.
     *
     * @param bot The bot instance to handle commands for.
     */
    public CommandsHandler(TelegramLongPollingBot bot) {
        this(new DefaultAuthorizer(), bot);
    }

    /**
     * Creates a command handler with a specific commands authorizer.
     *
     * @param authorizer The commands authorizer which determines if the user is allowed to use the command or not.
     */
    public CommandsHandler(Authorizer authorizer, TelegramLongPollingBot bot) {
        this.authorizer = authorizer;
        this.bot = bot;
    }

    public void registerCommand(Command command) {
        assert !commands.containsKey(command.name) : "A command under the same name '" + command.name + "' is already registered!";
        commands.put(command.name, command);
    }

    @Override
    public boolean process(Update event) {
        //FIXME: Implement the process method.
        return false;
    }
}
