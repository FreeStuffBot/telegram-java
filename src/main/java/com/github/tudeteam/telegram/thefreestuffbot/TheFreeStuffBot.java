package com.github.tudeteam.telegram.thefreestuffbot;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Command;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.CommandsHandler;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Locality;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Privacy;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.Authorizer;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.BasicAuthorizer;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.ConsumeOncePipe;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.Pipe;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TheFreeStuffBot extends TelegramLongPollingBot {

    /* Configuration variables using Environment variables */
    protected static final String botToken = System.getenv("BOT_TOKEN");
    protected static final String botUsername = System.getenv("BOT_USERNAME");
    //protected static final int botCreatorID = Integer.parseInt(System.getenv("BOT_CREATORID"));
    protected static final String botDatabaseURI = System.getenv("BOT_DATABASE");
    /* End of configuration */

    protected static final Pipe<Update> updatesPipe = new ConsumeOncePipe<>();

    public TheFreeStuffBot() {
        Authorizer authorizer = new BasicAuthorizer() {
            @Override
            public boolean isAdmin(Message message) {
                return false;
            }

            @Override
            public boolean isOwner(Message message) {
                return false;
            }

            @Override
            public boolean isGroupAdmin(Message message) {
                if (message.isUserMessage()) return true;
                try {
                    ChatMember member = execute(new GetChatMember()
                            .setChatId(message.getChatId())
                            .setUserId(message.getFrom().getId()));
                    return member.getStatus().equals("administrator") || member.getStatus().equals("creator");
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };

        CommandsHandler commandsHandler = new CommandsHandler(authorizer, this);
        updatesPipe.registerHandler(commandsHandler);

        Command pingCommand = new Command("ping", "Pong!", Locality.ALL, Privacy.PUBLIC) {
            @Override
            public void action(Message source) throws TelegramApiException {
                execute(new SendMessage().setChatId(source.getChatId()).setText("Pong 🏓"));
            }
        };

        commandsHandler.registerCommand(pingCommand);
    }

    /**
     * This method is called when receiving updates via GetUpdates method
     *
     * @param update Update received
     */
    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);
        System.out.println(updatesPipe.process(update) ? "The update got consumed!" : "The update was not consumed!");
    }

    /**
     * Return bot username of this bot
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Returns the token of the bot to be able to perform Telegram Api Requests
     *
     * @return Token of the bot
     */
    @Override
    public String getBotToken() {
        return botToken;
    }
}
