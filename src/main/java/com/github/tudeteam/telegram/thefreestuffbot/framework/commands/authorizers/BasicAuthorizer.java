package com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Command;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.ParsedCommand;

/**
 * Determines if a command can be executed according to its' locality and privacy levels.
 */
public interface BasicAuthorizer extends LocalityAuthorizer, PrivacyAuthorizer {

    @Override
    default String authorize(ParsedCommand parsedCommand, Command command) {
        String rejectionReason = checkLocality(parsedCommand, command);
        if (rejectionReason != null) return rejectionReason;

        rejectionReason = checkPrivacy(parsedCommand, command);
        return rejectionReason;
    }
}
