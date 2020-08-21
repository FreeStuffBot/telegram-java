package com.github.tudeteam.telegram.thefreestuffbot.framework.commands;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.Authorizer;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.DefaultAuthorizer;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.Handler;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public class CommandsHandler implements Handler<Update> {

    /**
     * Determines if a user can use a command or not.
     */
    protected final Authorizer authorizer;

    /**
     * Stores the registered commands.
     */
    protected final Map<String, Command> commands;

    /**
     * Executes the Telegram requests.
     */
    protected final SilentExecutor silent;

    /**
     * The username of the bot to process commands for.
     */
    protected final String botUsername;

    /**
     * Creates a commands handler with the default authorizer.
     * Which only authorizes {@code PUBLIC} commands.
     *
     * @param botUsername The username of the bot.
     * @param silent      A silent executor for the bot.
     */
    public CommandsHandler(String botUsername, SilentExecutor silent) {
        this(botUsername, silent, new DefaultAuthorizer());
    }

    /**
     * Creates a commands handler with the provided authorizer.
     *
     * @param botUsername The username of the bot.
     * @param silent      A silent executor for the bot.
     * @param authorizer  An authorizer for commands usage.
     */
    public CommandsHandler(String botUsername, SilentExecutor silent, Authorizer authorizer) {
        this(botUsername, silent, authorizer, new HashMap<>());
    }

    /**
     * Creates a commands handler with the provided authorizer and commands container.
     * This is supposed to be used by custom implementations so they can change the container (or null it too!).
     *
     * @param botUsername The username of the bot.
     * @param silent      A silent executor for the bot.
     * @param authorizer  An authorizer for commands usage.
     * @param commands    A map container for the commands.
     */
    protected CommandsHandler(String botUsername, SilentExecutor silent, Authorizer authorizer, Map<String, Command> commands) {
        this.botUsername = botUsername;
        this.silent = silent;
        this.authorizer = authorizer;
        this.commands = commands;
    }

    /**
     * Registers a command for usage by users.
     *
     * @param command The command to register.
     */
    public void registerCommand(Command command) {
        assert !commands.containsKey(command.name) : "There's a command under the same name '" + command.name + "'!";
        commands.put(command.name, command);
    }

    /**
     * Unregisters a command so it can't be used anymore.
     *
     * @param command The command to unregister.
     * @return {@code true} if the command was found and unregistered, {@code false} if it was not registered anyway.
     */
    public boolean unregisterCommand(Command command) {
        return commands.remove(command.name, command);
    }

    /**
     * Check if the command's locality allows using it.
     *
     * @param parsedCommand The parsed command request.
     * @param command       The command to execute.
     * @return {@code null} if it was valid, otherwise the rejection reason.
     */
    protected String checkCommandLocality(ParsedCommand parsedCommand, Command command) {
        //A group command used under private messages.
        if (command.locality == Locality.GROUP && parsedCommand.origin.isUserMessage())
            return parsedCommand + " is available only in groups ⚠";

        //A private messages command used under a group.
        if (command.locality == Locality.USER && !parsedCommand.origin.isUserMessage())
            return parsedCommand + " is available only in private chats ⚠";

        //The command's locality is valid.
        return null;
    }

    /**
     * Execute the command catching an exception necessary.
     *
     * @param message       The origin message of the command.
     * @param parsedCommand The parsed command request.
     * @param command       The command to execute.
     */
    protected void executeCommand(Message message, ParsedCommand parsedCommand, Command command) {
        try {
            command.action(message, parsedCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean process(Update update) {
        //Ignore non-message updates.
        if (!update.hasMessage()) return false;
        Message message = update.getMessage();

        //Ignore non-command messages.
        if (!message.isCommand()) return false;

        //Parse the command.
        ParsedCommand parsedCommand = ParsedCommand.parse(message);

        //Check the username in the command.
        if (parsedCommand.username == null) {
            if (!message.isUserMessage())
                return true; //The update got consumed, a command without the username postfix under a group.
        } else if (!parsedCommand.username.equals(botUsername)) {
            if (message.isUserMessage())
                silent.compose().text("You're requesting an another bot's command from me 😅") //EASTER_EGG
                        .chatId(message).send();
            return true; //The update got consumed, it's a command for an another bot.
        }

        //Lookup the command.
        Command command = commands.get(parsedCommand.name);

        //It was not found.
        if (command == null) {
            silent.compose().text("Unknown command " + parsedCommand + " ⚠").replyToOnlyInGroup(message).send();
            return true; //Update consumed, command not found.
        }

        //Check the locality of the command if allows using it.
        String localityRejection = checkCommandLocality(parsedCommand, command);
        if (localityRejection != null) {
            silent.compose().text(localityRejection).replyToOnlyInGroup(message).send();
            return true; //The update got consumed, the command's locality doesn't allow using it.
        }

        //Check the access level of the user if allows to use the command.
        String rejectionReason = authorizer.authorize(parsedCommand, command);
        if (rejectionReason != null) {
            silent.compose().text(rejectionReason).replyToOnlyInGroup(message).send();
            return true; //Update consumed, command not authorized.
        }

        //Execute the command.
        executeCommand(message, parsedCommand, command);

        return true; //Update consumed, command executed.
    }
}
