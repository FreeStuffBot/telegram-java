package com.github.tudeteam.telegram.thefreestuffbot.framework.commands;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.Authorizer;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.DefaultAuthorizer;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.Handler;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

    @Override
    public boolean process(Update update) {
        //TODO: Tear this method into multiple methods, and cleanup.

        //Ignore non-message updates.
        if (!update.hasMessage()) return false;
        Message message = update.getMessage();

        //Ignore non-text messages.
        if (!message.hasText()) return false;
        String text = message.getText();

        //Ignore messages not starting with a '/'.
        if (!text.startsWith("/")) return false;

        MessageEntity commandEntity = message.getEntities().get(0);
        if (!commandEntity.getType().equals("bot_command")) return false;
        if (commandEntity.getOffset() != 0) return false;
        String commandTag = text.substring(1, commandEntity.getLength());
        String commandName;

        if (commandTag.contains("@")) {
            int atPosition = commandTag.indexOf("@");
            commandName = commandTag.substring(0, atPosition);

            String commandUsername = commandTag.substring(atPosition + 1);
            if (!commandUsername.equals(botUsername)) {
                if (message.isUserMessage())
                    silent.compose().text("You're requesting an another bot's command from me 😅") //EASTER_EGG
                            .chatId(message).send();
                return true; //The update got consumed, it's a command for an another bot..
            }
        } else if (!message.isUserMessage()) {
            return true; //The update got consumed, a command without the username postfix under a group.
        } else {
            commandName = commandTag;
        }

        Command command = commands.get(commandName);

        if (command == null) {
            silent.compose().text("Unknown command /" + commandTag + " ⚠")
                    .replyToOnlyInGroup(message).send();

            return true; //Update consumed, command not found.
        }

        if (command.locality == Locality.GROUP && message.isUserMessage()) {
            silent.compose().text("/" + commandTag + " is available only in groups ⚠")
                    .chatId(message).send();

            return true; //Update consumed, a group command used under private messages.
        }

        if (command.locality == Locality.USER && !message.isUserMessage()) {
            silent.compose().text("/" + commandTag + " is available only in private chats ⚠")
                    .chatId(message).send();

            return true; //Update consumed, a private messages command used under a group.
        }


        String rejectionReason = authorizer.authorize(message, command);

        if (rejectionReason != null) {
            silent.compose().text(rejectionReason)
                    .replyToOnlyInGroup(message).send();

            return true; //Update consumed, command not authorized.
        }

        try {
            command.action(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return true; //Update consumed, command executed.
    }
}
