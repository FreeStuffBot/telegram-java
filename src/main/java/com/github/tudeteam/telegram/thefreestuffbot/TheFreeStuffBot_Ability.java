package com.github.tudeteam.telegram.thefreestuffbot;

import com.github.tudeteam.telegram.thefreestuffbot.structures.GameData;
import com.github.tudeteam.telegram.thefreestuffbot.structures.GameInfo;
import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.objects.Stats;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

public class TheFreeStuffBot_Ability extends AbilityBot {

    /* Configuration variables using Environment variables */
    protected static final String botToken = System.getenv("BOT_TOKEN");
    protected static final String botUsername = System.getenv("BOT_USERNAME");
    protected static final int botCreatorID = Integer.parseInt(System.getenv("BOT_CREATORID"));
    protected static final String botDatabaseURI = System.getenv("BOT_DATABASE");
    /* End of configuration */

    protected static final DefaultBotOptions options = new DefaultBotOptions();
    protected static final MongoClient mongoClient = MongoClients.create(botDatabaseURI);
    protected static final MongoDatabase mongoDatabase = mongoClient.getDatabase("freestuffbot");
    protected static final MongoCollection<Document> gamesCollection = mongoDatabase.getCollection("games");

    private static final Gson gson = new Gson();

    static {
        options.setMaxThreads(10);
    }

    protected TheFreeStuffBot_Ability() {
        super(botToken, botUsername, options);
    }

    static GameData getLatestAnnouncement() {
        Document document = gamesCollection
                .find(Filters.eq("status", "published"))
                .sort(Sorts.descending("published"))
                .first();

        assert document != null : "Announcement not found!";
        return gson.fromJson(document.toJson(), GameData.class);
    }

    @Override
    public int creatorId() {
        return botCreatorID;
    }

    @SuppressWarnings("unused")
    public Ability commandStart() {
        return Ability.builder()
                .name("start")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.USER)
                .action(ctx -> silent.send("⚠ The bot is work in progress ⚠", ctx.chatId()))
                .build();
    }

    @SuppressWarnings("unused")
    public Ability commandHere() {
        return Ability.builder()
                .name("here")
                .info("Only execute this command when prompted to do so by the FreeStuff support team!")
                .privacy(Privacy.PUBLIC)
                .locality(Locality.ALL)
                .action(ctx -> {
                    System.out.println("SUPPORT WAS CALLED: chatID: " + ctx.chatId());
                    silent.send("Help is on the way!", ctx.chatId());
                })
                .enableStats()
                .build();
    }

    public Ability commandTest() {
        return Ability.builder()
                .name("test")
                .info("Execute a test announcement.")
                .privacy(Privacy.ADMIN)
                .locality(Locality.ALL)
                .action(ctx -> {
                    GameData game = getLatestAnnouncement();
                    GameInfo info = game.info;

                    InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
                    inlineMarkup.getKeyboard().add(List.of(new InlineKeyboardButton()
                            .setText("Get")
                            .setUrl(info.org_url.toString())
                    ));

                    try {
                        execute(new SendPhoto()
                                .setChatId(ctx.chatId())
                                .setPhoto(info.thumbnail.toString())
                                .setCaption(String.format("<b>Free Game!</b>\n<b>%s</b>\n<s>$%s</s> <b>Free</b> • %s\nvia freestuffbot.xyz", info.title, info.org_price.dollar, info.store.name()))
                                .setParseMode("HTML")
                                .setReplyMarkup(inlineMarkup)
                        );
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                })
                .build();
    }

    @Override
    public Map<String, Stats> stats() {
        return super.stats();
    }
}
