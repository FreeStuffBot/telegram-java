package com.github.tudeteam.telegram.thefreestuffbot;

import com.github.tudeteam.telegram.thefreestuffbot.announcements.Announcements;
import com.github.tudeteam.telegram.thefreestuffbot.announcements.DatabaseWatcher;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Command;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.CommandsHandler;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Locality;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Privacy;
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
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.mongodb.client.model.Filters.*;
import static java.util.concurrent.TimeUnit.MINUTES;

public class TheFreeStuffBot extends TelegramLongPollingBot {

    /* Configuration variables using Environment variables */
    protected static final String botToken = System.getenv("BOT_TOKEN");
    protected static final String botUsername = System.getenv("BOT_USERNAME");
    protected static final int botCreatorID = Integer.parseInt(System.getenv("BOT_CREATORID"));
    protected static final String botDatabaseURI = System.getenv("BOT_DATABASE");
    /* End of configuration */
    private static final Gson gson = new Gson();
    protected static final MongoClient mongoClient = MongoClients.create(botDatabaseURI);
    protected static final MongoDatabase mongoDatabase = mongoClient.getDatabase("freestuffbot");
    protected static final MongoCollection<Document> adminsCollection = mongoDatabase.getCollection("telegram-admins");
    protected static final MongoCollection<Document> chatsCollection = mongoDatabase.getCollection("telegram-chats");
    protected static final MongoCollection<Document> configCollection = mongoDatabase.getCollection("telegram-config");
    protected static final MongoCollection<Document> gamesCollection = mongoDatabase.getCollection("games");
    protected static final MongoCollection<Document> ongoingCollection = mongoDatabase.getCollection("telegram-ongoing");
    protected static final MongoCollection<Document> processedCollection = mongoDatabase.getCollection("telegram-processed");
    private static final DefaultBotOptions botOptions = new DefaultBotOptions();

    static {
        botOptions.setMaxThreads(6);
    }

    protected final Pipe<Update> updatesPipe = new ConsumeOncePipe<>();
    protected final SilentExecutor silent = new SilentExecutor(this);
    protected final AuthorizeWithMongoDB authorizer = new AuthorizeWithMongoDB(silent, botCreatorID, adminsCollection);
    protected final CommandsHandler commandsHandler = new CommandsHandler(botUsername, silent, authorizer);
    protected final ChatsTracker chatsTracker = new ChatsTracker(botUsername, chatsCollection);
    protected final ConfigurationDB configurationDB = new ConfigurationDB(configCollection);
    protected final MenuHandler menuHandler = new MenuHandler(silent, configurationDB, authorizer);
    protected final Announcements announcements = new Announcements(botCreatorID, this.exe, silent, configurationDB, ongoingCollection, processedCollection, gamesCollection);
    protected final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public TheFreeStuffBot() {
        super(botOptions);
        scheduledExecutor.scheduleAtFixedRate(new DatabaseWatcher(announcements, configCollection, gamesCollection, ongoingCollection),
                0, 1, MINUTES);
        announcements.wakeUp();

        updatesPipe.registerHandler(chatsTracker);
        updatesPipe.registerHandler(commandsHandler);
        updatesPipe.registerHandler(menuHandler);

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
                    InlineKeyboardMarkup markup = menuHandler.constructMenuKeyboard(message.getChatId());

                    silent.compose().text("Configuration menu ⚙")
                            .markup(markup).chatId(message).send();
                })
                .build();

        commandsHandler.newCommand()
                .name("update_commands")
                .description("Update the public commands definition of the bot ⚙")
                .privacy(Privacy.ADMIN)
                .action((message, parsedCommand) -> {
                    Command[] commands = commandsHandler.getCommands();
                    List<BotCommand> botCommands = new ArrayList<>();

                    for (Command command : commands) {
                        if (command.locality == Locality.ALL) {
                            if (command.privacy == Privacy.PUBLIC || command.privacy == Privacy.GROUP_ADMIN) {
                                if (command.description != null) {
                                    botCommands.add(new BotCommand()
                                            .setCommand(command.name)
                                            .setDescription(command.description));
                                }
                            }
                        }
                    }

                    boolean success = silent.execute(new SetMyCommands().setCommands(botCommands));
                    silent.compose().text(success ? "Updated commands definition successfully ✅" : "Failed to update commands definition ⚠")
                            .replyToOnlyInGroup(message).send();
                })
                .build();

        commandsHandler.newCommand()
                .name("announce_test")
                .description("Send a test announcements to everyone 🙃")
                .privacy(Privacy.ADMIN)
                .action((message, parsedCommand) -> {
                    Document document = new Document();
                    document.put("startedAt", (int) (System.currentTimeMillis() / 1000L));
                    document.put("finishedAt", null);
                    document.put("type", "TEST");
                    document.put("data", new Document("content", "This is a test announcements ℹ"));

                    List<Long> pendingChats = new ArrayList<>();

                    configCollection.find(eq("enabled", true)).forEach(config ->
                            pendingChats.add(config.getLong("_id")));

                    document.put("chats", new Document("pending", pendingChats)
                            .append("processed", new ArrayList<>())
                            .append("failed", new ArrayList<>()));

                    document.put("reached", new Document("users", 0)
                            .append("groups", 0)
                            .append("supergroups", 0)
                            .append("channels", 0));

                    boolean success = ongoingCollection.insertOne(document).wasAcknowledged();

                    silent.compose().text(success ? "Created a test announcement successfully ✅" : "Failed to create a test announcement ⚠")
                            .replyToOnlyInGroup(message).send();

                    announcements.wakeUp();
                })
                .build();

        commandsHandler.newCommand()
                .name("wake_up")
                .description("Wakeup the announcements worker")
                .privacy(Privacy.ADMIN)
                .action((message, parsedCommand) -> {
                    announcements.wakeUp();
                    silent.compose().text("Woke up the worker successfully ✅")
                            .replyToOnlyInGroup(message).send();
                }).build();

        commandsHandler.newCommand()
                .name("requeue_failed")
                .description("Requeue failed announcements")
                .privacy(Privacy.ADMIN)
                .action((message, parsedCommand) -> {
                    List<Document> toRequeue = new ArrayList<>();

                    processedCollection.find(not(size("chats.failed", 0))).forEach(document -> {
                        Document chats = document.get("chats", Document.class);
                        chats.put("pending", chats.getList("failed", Long.class));
                        chats.put("failed", new ArrayList<>());

                        toRequeue.add(document);
                        processedCollection.deleteOne(eq("_id", document.get("_id")));
                    });

                    if (toRequeue.isEmpty())
                        silent.compose().text("There are no failed announcements to requeue 😌")
                                .replyToOnlyInGroup(message).send();
                    else {
                        ongoingCollection.insertMany(toRequeue);
                        silent.compose().text("Requeued " + toRequeue.size() + " failed announcements ✅")
                                .replyToOnlyInGroup(message).send();
                    }

                    announcements.wakeUp();
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
