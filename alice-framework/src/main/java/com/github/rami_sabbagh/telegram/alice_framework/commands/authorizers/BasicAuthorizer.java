package com.github.rami_sabbagh.telegram.alice_framework.commands.authorizers;

import com.github.rami_sabbagh.telegram.alice_framework.commands.Command;
import com.github.rami_sabbagh.telegram.alice_framework.commands.ParsedCommand;

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
