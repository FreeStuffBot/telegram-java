package com.github.tudeteam.telegram.thefreestuffbot;

import com.github.rami_sabbagh.telegram.alice_framework.bots.alice.AliceBot;
import com.github.rami_sabbagh.telegram.alice_framework.commands.Privacy;
import com.github.tudeteam.telegram.thefreestuffbot.components.ConfigurationDB;
import com.github.tudeteam.telegram.thefreestuffbot.components.InlineQueryHandler;
import com.github.tudeteam.telegram.thefreestuffbot.components.announcements.AnnouncementsMigrationHandler;
import com.github.tudeteam.telegram.thefreestuffbot.components.announcements.CheckDatabase;
import com.github.tudeteam.telegram.thefreestuffbot.components.settings.SettingsMenu;
import com.github.tudeteam.telegram.thefreestuffbot.structures.ChatConfiguration;
import com.github.tudeteam.telegram.thefreestuffbot.structures.GameData;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.set;
import static java.util.concurrent.TimeUnit.MINUTES;

public class TheFreeStuffBot extends AliceBot {

    private static final Gson gson = new Gson();

    /* MongoDB Collections */
    public final MongoCollection<Document> configCollection = mongoDatabase.getCollection("telegram-config");
    public final MongoCollection<Document> gamesCollection = mongoDatabase.getCollection("games");
    /* Bot Components */
    public final ConfigurationDB configurationDB = new ConfigurationDB(this);
    public final InlineQueryHandler inlineQueryHandler = new InlineQueryHandler(this);
    public final SettingsMenu settingsMenu = new SettingsMenu(this);
    public final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public TheFreeStuffBot() {
        super(new TheFreeStuffBotOptions());
        updatesPipe.registerHandler(new AnnouncementsMigrationHandler(this));
        updatesPipe.registerHandler(configurationDB.migrationHandler);
        updatesPipe.registerHandler(inlineQueryHandler);
        updatesPipe.registerHandler(settingsMenu);

        scheduledExecutor.scheduleWithFixedDelay(new CheckDatabase(this), 0, 1, MINUTES);

        commandsHandler.newCommand()
                .name("free")
                .description("List currently free games")
                .action((message, parsedCommand) -> {
                    long chatId = message.getChatId();
                    ChatConfiguration config = configurationDB.getConfigurationWithDefaultFallback(chatId);

                    List<String> games = new ArrayList<>();
                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();

                    int currentTime = (int) (System.currentTimeMillis() / 1000L);
                    gamesCollection.find(and(
                            eq("status", "published"),
                            gte("info.until", currentTime)
                    )).sort(descending("published"))
                            .forEach(document -> {
                                GameData gameData = gson.fromJson(document.toJson(), GameData.class);
                                games.add("• " + gameData.info.formatCaptionWithoutHeaderAndFooter(config));

                                keyboard.add(List.of(new InlineKeyboardButton()
                                        .setText("Share details • " + gameData.info.title)
                                        .setSwitchInlineQuery("game_id:" + gameData._id)));
                            });

                    if (games.isEmpty())
                        silent.compose().text("There are no free games currently 😕")
                                .chatId(message).send();
                    else
                        silent.compose().html("<b>These games are currently free:</b>\n\n"
                                + String.join("\n\n", games)
                                + "\n\n<i>via freestuffbot.xyz</i>")
                                .disableWebPagePreview().markup(markup)
                                .chatId(message).send();

                })
                .build();

        commandsHandler.newCommand()
                .name("menu")
                .description("Configure the bot ⚙")
                .action((message, parsedCommand) -> {
                    InlineKeyboardMarkup markup = settingsMenu.constructSettingsMenuMarkup(message.getChatId());

                    silent.compose().text("Configuration menu ⚙")
                            .markup(markup).chatId(message).send();
                })
                .build();

        commandsHandler.newCommand()
                .name("announce_all")
                .description("Announce all the published games in the database ⚛")
                .privacy(Privacy.ADMIN)
                .action((message, parsedCommand) -> {
                    long count = gamesCollection.updateMany(eq("status", "published"), set("outgoing", new Document("telegram", true))).getMatchedCount();

                    silent.compose().text(count != 0 ? ("Announcing " + count + " games ✅") : "Failed to announce any games ⚠")
                            .replyToOnlyInGroup(message).send();
                })
                .build();

        commandsHandler.newCommand()
                .name("announce")
                .description("Announce a specific game by id 📢")
                .privacy(Privacy.ADMIN)
                .action((message, parsedCommand) -> {
                    String parameters = parsedCommand.parameters;
                    if (parameters.isBlank()) {
                        silent.compose().text("Usage " + parsedCommand + " <game_id>")
                                .replyToOnlyInGroup(message).send();
                        return;
                    }

                    long gameId;

                    try {
                        gameId = Long.parseLong(parameters);
                    } catch (NumberFormatException e) {
                        silent.compose().text("Invalid game id ⚠")
                                .replyToOnlyInGroup(message).send();
                        return;
                    }

                    boolean success = gamesCollection.updateOne(eq("_id", gameId),
                            set("outgoing", new Document("telegram", true))).getMatchedCount() == 1;

                    silent.compose().text(success ? "Announced successfully ✅" : "Game not found ⚠")
                            .replyToOnlyInGroup(message).send();
                })
                .build();
    }


    /**
     * Shutdown the bot.
     */
    @Override
    public void onClosing() {
        //Executors
        scheduledExecutor.shutdown();
        //AliceBot
        super.onClosing();
    }
}
