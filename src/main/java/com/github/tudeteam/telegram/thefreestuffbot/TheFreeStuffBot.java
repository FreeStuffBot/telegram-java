package com.github.tudeteam.telegram.thefreestuffbot;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.CommandsHandler;
import com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.ChatsTracker;
import com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.commands.DemoteCommand;
import com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.commands.PromoteCommand;
import com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.commands.authorizers.AuthorizeWithMongoDB;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.ConsumeOncePipe;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.Pipe;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import com.github.tudeteam.telegram.thefreestuffbot.structures.GameData;
import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

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
    protected static final MongoCollection<Document> gamesCollection = mongoDatabase.getCollection("games");
    protected final Pipe<Update> updatesPipe = new ConsumeOncePipe<>();
    protected final SilentExecutor silent = new SilentExecutor(this);
    protected final AuthorizeWithMongoDB authorizer = new AuthorizeWithMongoDB(botCreatorID, silent, adminsCollection);
    protected final CommandsHandler commandsHandler = new CommandsHandler(botUsername, silent, authorizer);
    protected final ChatsTracker chatsTracker = new ChatsTracker(botUsername, chatsCollection);
    Gson gson = new Gson();

    public TheFreeStuffBot() {
        updatesPipe.registerHandler(chatsTracker);
        updatesPipe.registerHandler(commandsHandler);

        commandsHandler.registerCommand(new PromoteCommand(adminsCollection, silent, botCreatorID));
        commandsHandler.registerCommand(new DemoteCommand(adminsCollection, silent, botCreatorID));

        commandsHandler.newCommand()
                .name("ping")
                .description("Pong 🏓")
                .action((message, parsedCommand) -> silent.compose().text("Pong 🏓")
                        .chatId(message).send())
                .build();

        commandsHandler.newCommand()
                .name("free")
                .description("List currently free games")
                .action((message, parsedCommand) -> {
                    int currentTime = (int) (System.currentTimeMillis() / 1000L);

                    List<String> games = new ArrayList<>();

                    gamesCollection.find(and(
                            eq("status", "published"),
                            gte("info.until", currentTime)
                    )).sort(new Document("published", -1))
                            .forEach(document -> {
                                GameData gameData = gson.fromJson(document.toJson(), GameData.class);
                                games.add("• " + gameData.info.title);
                            });

                    if (games.isEmpty())
                        silent.compose().text("There are no free games currently 😕")
                                .chatId(message).send();
                    else
                        silent.compose().markdown("*Those games are currently free:*\n" + String.join("\n", games))
                                .chatId(message).send();

                })
                .build();

        commandsHandler.newCommand()
                .name("menu")
                .description("Configure the bot ⚙")
                .action((message, parsedCommand) -> {

                    InlineKeyboardMarkup notConfigured = new InlineKeyboardMarkup();
                    notConfigured.getKeyboard().add(List.of(new InlineKeyboardButton()
                            .setText("Enable announcements 📢")
                            .setCallbackData("menu:announcements-enable")
                    ));

                    silent.compose().text("Configuration menu ⚙")
                            .markup(notConfigured).chatId(message).send();

                    InlineKeyboardMarkup configured = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> configuredKeyboard = configured.getKeyboard();

                    configuredKeyboard.add(List.of(new InlineKeyboardButton()
                            .setText("Disable announcements ⚠")
                            .setCallbackData("menu:announcements-disable")
                    ));
                    configuredKeyboard.add(List.of(new InlineKeyboardButton()
                            .setText("Currency used: $ - switch to €")
                            .setCallbackData("menu:currency-set-eur")
                    ));
                    configuredKeyboard.add(List.of(new InlineKeyboardButton()
                            .setText("Until format: date - switch to weekday")
                            .setCallbackData("menu:until-set-weekday")
                    ));
                    configuredKeyboard.add(List.of(new InlineKeyboardButton()
                            .setText("Filter low-quality games: ✅ - toggle")
                            .setCallbackData("menu:trash-disable")
                    ));
                    configuredKeyboard.add(List.of(new InlineKeyboardButton()
                            .setText("Minimal original price: $5 - change")
                            .setCallbackData("menu:min-price-set")
                    ));
                    configuredKeyboard.add(List.of(new InlineKeyboardButton()
                                    .setText("Support bot ♥")
                                    .setCallbackData("menu:support-bot")
                            , new InlineKeyboardButton()
                                    .setText("Configuration help ℹ")
                                    .setCallbackData("menu:help")
                    ));

                    silent.compose().text("Configuration menu ⚙")
                            .markup(configured).chatId(message).send();

                    InlineKeyboardMarkup deConfigured = new InlineKeyboardMarkup();
                    deConfigured.getKeyboard().add(List.of(new InlineKeyboardButton()
                            .setText("Enable announcements 📢")
                            .setCallbackData("menu:announcements-enable")
                    ));
                    deConfigured.getKeyboard().add(List.of(new InlineKeyboardButton()
                            .setText("Delete configuration ♻")
                            .setCallbackData("menu:announcements-enable")
                    ));

                    silent.compose().text("Configuration menu ⚙")
                            .markup(deConfigured).chatId(message).send();

                })
                .build();
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
