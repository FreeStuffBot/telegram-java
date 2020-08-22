package com.github.tudeteam.telegram.thefreestuffbot;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.CommandsHandler;
import com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.ChatsTracker;
import com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.commands.DemoteCommand;
import com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.commands.PromoteCommand;
import com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.commands.authorizers.AuthorizeWithMongoDB;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.ConsumeOncePipe;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.Pipe;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TheFreeStuffBot extends TelegramLongPollingBot {

    /* Configuration variables using Environment variables */
    protected static final String botToken = System.getenv("BOT_TOKEN");
    protected static final String botUsername = System.getenv("BOT_USERNAME");
    protected static final int botCreatorID = Integer.parseInt(System.getenv("BOT_CREATORID"));
    protected static final String botDatabaseURI = System.getenv("BOT_DATABASE");
    /* End of configuration */

    protected static final MongoClient mongoClient = MongoClients.create(botDatabaseURI);
    protected static final MongoDatabase mongoDatabase = mongoClient.getDatabase("freestuffbot");
    protected static final MongoCollection<Document> adminsCollection = mongoDatabase.getCollection("telegram-admins");
    protected static final MongoCollection<Document> chatsCollection = mongoDatabase.getCollection("telegram-chats");

    protected final Pipe<Update> updatesPipe = new ConsumeOncePipe<>();
    protected final SilentExecutor silent = new SilentExecutor(this);
    protected final AuthorizeWithMongoDB authorizer = new AuthorizeWithMongoDB(botCreatorID, silent, adminsCollection);
    protected final CommandsHandler commandsHandler = new CommandsHandler(botUsername, silent, authorizer);
    protected final ChatsTracker chatsTracker = new ChatsTracker(botUsername, chatsCollection);

    public TheFreeStuffBot() {
        updatesPipe.registerHandler(chatsTracker);
        updatesPipe.registerHandler(commandsHandler);

        commandsHandler.newCommand()
                .name("ping")
                .description("Pong 🏓")
                .action((message, parsedCommand) -> silent.compose().text("Pong 🏓")
                        .replyToOnlyInGroup(message).send())
                .build();

        commandsHandler.registerCommand(new PromoteCommand(adminsCollection, silent, botCreatorID));
        commandsHandler.registerCommand(new DemoteCommand(adminsCollection, silent, botCreatorID));
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
