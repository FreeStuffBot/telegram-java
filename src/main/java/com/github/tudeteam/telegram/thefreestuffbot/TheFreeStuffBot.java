package com.github.tudeteam.telegram.thefreestuffbot;

import com.github.rami_sabbagh.telegram.alice_framework.bots.alice.AliceBot;
import com.github.rami_sabbagh.telegram.alice_framework.commands.Command;
import com.github.rami_sabbagh.telegram.alice_framework.commands.Locality;
import com.github.rami_sabbagh.telegram.alice_framework.commands.Privacy;
import com.github.tudeteam.telegram.thefreestuffbot.announcements.CheckDatabase;
import com.github.tudeteam.telegram.thefreestuffbot.structures.GameData;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static java.util.concurrent.TimeUnit.MINUTES;

public class TheFreeStuffBot extends AliceBot {

    /* End of configuration */
    private static final Gson gson = new Gson();
    /* MongoDB */
    protected final MongoCollection<Document> configCollection = mongoDatabase.getCollection("telegram-config");
    protected final MongoCollection<Document> gamesCollection = mongoDatabase.getCollection("games");
    protected final ConfigurationDB configurationDB = new ConfigurationDB(configCollection);
    protected final MenuHandler menuHandler = new MenuHandler(silent, configurationDB, authorizer);
    protected final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public TheFreeStuffBot() {
        super(new TheFreeStuffBotOptions());

        scheduledExecutor.scheduleWithFixedDelay(new CheckDatabase(silent, this.exe, configCollection, gamesCollection, redisCommands),
                0, 1, MINUTES);

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
