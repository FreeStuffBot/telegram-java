package com.github.tudeteam.telegram.thefreestuffbot.framework.commands;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.Authorizer;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.DefaultAuthorizer;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.Handler;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

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
     * @param bot        The bot instance to handle commands for.
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
            if (!commandUsername.equals(bot.getBotUsername())) {
                if (message.isUserMessage()) {
                    try { //TODO: Use a "silent" message sender.
                        bot.execute(new SendMessage()
                                .setChatId(message.getChatId())
                                .setText("You're requesting an another bot's command from me 😅")); //EASTER_EGG
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                return true; //The update got consumed, it's a command for an another bot..
            }
        } else if (!message.isUserMessage()) {
            return true; //The update got consumed, a command without the username postfix under a group.
        } else {
            commandName = commandTag;
        }

        Command command = commands.get(commandName);

        if (command == null) {
            try { //TODO: Use a "silent" message sender.
                bot.execute(new SendMessage()
                        .setChatId(message.getChatId())
                        .setReplyToMessageId(message.isGroupMessage() ? message.getMessageId() : null)
                        .setText("Unknown command /" + commandTag + " ⚠"));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            return true; //Update consumed, command not found.
        }

        if (command.locality == Locality.GROUP && message.isUserMessage()) {
            try {
                bot.execute(new SendMessage()
                        .setChatId(message.getChatId())
                        .setText("/" + commandTag + " is available only in groups ⚠"));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            return true; //Update consumed, a group command used under private messages.
        }

        if (command.locality == Locality.USER && !message.isUserMessage()) {
            try {
                bot.execute(new SendMessage()
                        .setChatId(message.getChatId())
                        .setText("/" + commandTag + " is available only in private chats ⚠"));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            return true; //Update consumed, a private messages command used under a group.
        }


        String rejectionReason = authorizer.authorize(message, command);

        if (rejectionReason != null) {
            try { //TODO: Use a "silent" message sender.
                bot.execute(new SendMessage()
                        .setChatId(message.getChatId())
                        .setReplyToMessageId(message.isGroupMessage() ? message.getMessageId() : null)
                        .setText(rejectionReason));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

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
