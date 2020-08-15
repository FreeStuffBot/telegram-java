package com.github.tudeteam.telegram.thefreestuffbot;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class TheFreeStuffBot extends AbilityBot {

    /* Configuration variables using Environment variables */
    protected static final String botToken = System.getenv("BOT_TOKEN");
    protected static final String botUsername = System.getenv("BOT_USERNAME");
    protected static final int botCreatorID = Integer.parseInt(System.getenv("BOT_CREATORID"));
    protected static final String botDatabaseURI = System.getenv("BOT_DATABASE");
    /* End of configuration */

    protected static final DefaultBotOptions options = new DefaultBotOptions();

    static {
        options.setMaxThreads(10);
    }

    protected TheFreeStuffBot() {
        super(botToken, botUsername, options);
    }

    @Override
    public int creatorId() {
        return botCreatorID;
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
                .build();
    }
}
