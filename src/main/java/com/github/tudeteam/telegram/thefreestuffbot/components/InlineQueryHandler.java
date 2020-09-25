package com.github.tudeteam.telegram.thefreestuffbot.components;

import com.github.rami_sabbagh.telegram.alice_framework.pipes.Handler;
import com.github.rami_sabbagh.telegram.alice_framework.utilities.SilentExecutor;
import com.github.tudeteam.telegram.thefreestuffbot.TheFreeStuffBot;
import com.github.tudeteam.telegram.thefreestuffbot.structures.ChatConfiguration;
import com.github.tudeteam.telegram.thefreestuffbot.structures.GameData;
import com.github.tudeteam.telegram.thefreestuffbot.structures.GameFlag;
import com.github.tudeteam.telegram.thefreestuffbot.structures.UntilFormat;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;

public class InlineQueryHandler implements Handler<Update> {

    protected static Gson gson = new Gson();

    protected final ConfigurationDB db;
    protected final SilentExecutor silent;
    protected final MongoCollection<Document> gamesCollection;

    public InlineQueryHandler(TheFreeStuffBot bot) {
        db = bot.configurationDB;
        silent = bot.silent;
        gamesCollection = bot.gamesCollection;
    }

    private static String searchRegex(String input) {
        return "(?i)" + input.replaceAll("[-.\\+*?\\[^\\]$(){}=!<>|:\\\\]", "\\\\$0");
    }

    @Override
    public boolean process(Update update) {
        if (!update.hasInlineQuery()) return false;
        InlineQuery query = update.getInlineQuery();
        User from = query.getFrom();

        AnswerInlineQuery response = new AnswerInlineQuery();
        response.setInlineQueryId(query.getId());

        List<InlineQueryResult> results = new ArrayList<>();
        response.setCacheTime(0);
        response.setResults(results);
        response.setPersonal(true);
        response.setNextOffset("");

        ChatConfiguration tempConfig = db.getConfiguration(from.getId());
        if (tempConfig == null) tempConfig = ChatConfiguration.defaultConfig;
        ChatConfiguration config = tempConfig;

        int currentTime = (int) (System.currentTimeMillis() / 1000L);
        gamesCollection.find(and(
                regex("info.title", searchRegex(query.getQuery())),
                or(eq("status", "published"), eq("status", "accepted")),
                gte("info.until", currentTime)
        )).sort(descending("published")).limit(50)
                .forEach(document -> {
                    GameData gameData = gson.fromJson(document.toJson(), GameData.class);
                    if (!config.trash && gameData.info.hasFlag(GameFlag.TRASH)) return;
                    if (config.minPrice > gameData.info.price.inCurrency(config.currency)) return;

                    InlineQueryResultPhoto result = new InlineQueryResultPhoto();

                    InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
                    inlineMarkup.getKeyboard().add(List.of(new InlineKeyboardButton()
                            .setText("Get")
                            .setUrl(gameData.info.org_url.toString())));

                    result.setId(String.valueOf(gameData._id));
                    result.setPhotoUrl(gameData.info.thumbnail.toString());
                    result.setThumbUrl(gameData.info.thumbnail.toString());
                    result.setTitle(gameData.info.title);
                    result.setDescription("Free until " + gameData.info.formatUntil(UntilFormat.DATE)
                            + " • " + gameData.info.store.toString());
                    result.setCaption(gameData.info.formatCaption(config));
                    result.setParseMode("HTML");
                    result.setReplyMarkup(inlineMarkup);

                    results.add(result);
                });

        silent.execute(response);
        return true;
    }
}
