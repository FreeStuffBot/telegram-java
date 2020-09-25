package com.github.tudeteam.telegram.thefreestuffbot.settings;

import com.github.rami_sabbagh.telegram.alice_framework.commands.authorizers.PrivacyAuthorizer;
import com.github.rami_sabbagh.telegram.alice_framework.interactivity.InteractivityListener;
import com.github.rami_sabbagh.telegram.alice_framework.interactivity.InteractivityState;
import com.github.rami_sabbagh.telegram.alice_framework.utilities.SilentExecutor;
import com.github.tudeteam.telegram.thefreestuffbot.ConfigurationDB;
import com.github.tudeteam.telegram.thefreestuffbot.TheFreeStuffBot;
import com.github.tudeteam.telegram.thefreestuffbot.structures.Currency;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MinimumPricePrompt implements InteractivityListener {

    protected static ForceReplyKeyboard forceReply = new ForceReplyKeyboard();
    protected static InlineKeyboardMarkup returnToSettings = new InlineKeyboardMarkup();

    static {
        returnToSettings.getKeyboard().add(SettingsMenu.SettingsButton.RETURN_TO_SETTINGS.buttonRow);
    }

    protected final PrivacyAuthorizer authorizer;
    protected final SilentExecutor silent;
    protected final ConfigurationDB db;

    public MinimumPricePrompt(TheFreeStuffBot bot) {
        authorizer = bot.authorizer;
        silent = bot.silent;
        db = bot.configurationDB;
    }

    @Override
    public void activated(long chatId, InteractivityState state) {
        state.setFilterInGroups(silent.compose().text("Please input the minimum price:")
                .chatId(chatId).markup(forceReply).send());
    }

    @Override
    public boolean process(long chatId, Message message, InteractivityState state) {
        if (!authorizer.isGroupAdmin(message) && !authorizer.isAdmin(message))
            return false; //Ignore the replies from non-admins.

        if (!message.hasText()) {
            return state.setFilterInGroups(silent.compose()
                    .text("Please input the minimum price:")
                    .replyToOnlyInGroup(message).markup(forceReply).send());
        }

        Currency currency = db.exists(chatId) ? db.getConfiguration(chatId).currency : Currency.USD;
        String text = message.getText();
        double value;

        try {
            value = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            silent.compose().html("<b>Invalid number ⚠</b>\nPlease input a valid minimum price:")
                    .replyTo(message).markup(forceReply).send();
            return true;
        }

        String price = String.format("%s%04.2f", currency, value);

        if (value < 0) {
            return state.setFilterInGroups(silent.compose()
                    .html("<b>How? What? Why?</b>\n"
                            + value + " is a negative value. This doesn't make sense."
                            + " Please choose 0 if you don't want any price restrictions"
                            + " or a valid positive number otherwise!\n\n"
                            + "Please input a valid minimum price:")
                    .replyToOnlyInGroup(message).markup(forceReply).send());

        } else if (value == 0) {
            silent.compose().html("<b>As you wish, no price filter!</b\n>"
                    + "Now each and every game will be announced, no matter how expensive it is."
                    + " Or better: was")
                    .replyToOnlyInGroup(message).markup(returnToSettings).send();

        } else if (value == 69) {
            silent.compose().html("<b>Nice!</b>\n"
                    + "As you wish, no price filter!")
                    .replyToOnlyInGroup(message).markup(returnToSettings).send();

        } else if (value > 100) {
            return state.setFilterInGroups(silent.compose()
                    .html("<b>Let's not get ridiculous!</b>\n"
                            + "Which game that costs over " + price + " will ever be free?\n\n"
                            + "Choose something more reasonable please:")
                    .replyToOnlyInGroup(message).markup(forceReply).send());

        } else {
            silent.compose().html("<b>Excellent choice!</b>\n"
                    + "Every game cheaper than " + price + " will no longer make it to this chat!")
                    .replyToOnlyInGroup(message).markup(returnToSettings).send();
        }

        db.setMinPrice(chatId, value);
        state.finished = true;
        return true;
    }

    @Override
    public void deactivated(long chatId, InteractivityState state) {
        silent.compose().text("Canceled minimum price set successfully ✅")
                .chatId(chatId).send();
    }
}
