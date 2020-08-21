package com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Command;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.ParsedCommand;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Privacy;

/**
 * Authorizes all the requests of public commands, and rejects the rest.
 */
public class DefaultAuthorizer implements Authorizer {

    @Override
    public String authorize(ParsedCommand parsedCommand, Command command) {
        return (command.privacy == Privacy.PUBLIC ? null : "No one is authorized to use this command!");
    }
}
