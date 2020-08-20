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
     * Creates a command handler with public commands authorization only.
     */
    public CommandsHandler() {
        authorizer = new DefaultAuthorizer();
    }

    /**
     * Creates a command handler with a specific commands authorizer.
     *
     * @param authorizer The commands authorizer which determines if the user is allowed to use the command or not.
     */
    public CommandsHandler(Authorizer authorizer) {
        this.authorizer = authorizer;
    }

    @Override
    public boolean process(Update event) {
        //FIXME: Implement the process method.
        return false;
    }
}
