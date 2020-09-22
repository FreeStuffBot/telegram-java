package com.github.tudeteam.telegram.thefreestuffbot;

import com.github.rami_sabbagh.telegram.alice_framework.bots.alice.AliceBot;
import com.github.rami_sabbagh.telegram.alice_framework.commands.Privacy;
import com.github.tudeteam.telegram.thefreestuffbot.announcements.CheckDatabase;
import com.github.tudeteam.telegram.thefreestuffbot.structures.GameData;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static java.util.concurrent.TimeUnit.MINUTES;

public class TheFreeStuffBot extends AliceBot {

    private static final Gson gson = new Gson();

    /* MongoDB Collections */
    protected final MongoCollection<Document> configCollection = mongoDatabase.getCollection("telegram-config");
    protected final MongoCollection<Document> gamesCollection = mongoDatabase.getCollection("games");
    /* Bot Components */
    protected final ConfigurationDB configurationDB = new ConfigurationDB(configCollection);
    protected final MenuHandler menuHandler = new MenuHandler(silent, configurationDB, authorizer);
    protected final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public TheFreeStuffBot() {
        super(new TheFreeStuffBotOptions());
        updatesPipe.registerHandler(menuHandler);

        scheduledExecutor.scheduleWithFixedDelay(new CheckDatabase(silent, this.exe, configCollection, gamesCollection, redisCommands),
                0, 1, MINUTES);

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
                                games.add("• " + gameData.info.title.replace("!", "\\!"));
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
                .name("announce_all")
                .description("Announce all the published games in the database ⚛")
                .privacy(Privacy.ADMIN)
                .action((message, parsedCommand) -> {
                    long count = gamesCollection.updateMany(eq("status", "published"), set("outgoing", new Document("telegram", true))).getMatchedCount();

                    silent.compose().text(count != 0 ? ("Announcing " + count + " games ✅") : "Failed to announce any games ⚠")
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
