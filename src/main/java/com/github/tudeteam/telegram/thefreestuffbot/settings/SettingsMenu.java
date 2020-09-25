package com.github.tudeteam.telegram.thefreestuffbot.settings;

import com.github.rami_sabbagh.telegram.alice_framework.utilities.SilentExecutor;
import com.github.tudeteam.telegram.thefreestuffbot.ConfigurationDB;
import com.github.tudeteam.telegram.thefreestuffbot.TheFreeStuffBot;
import com.github.tudeteam.telegram.thefreestuffbot.structures.ChatConfiguration;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static com.github.tudeteam.telegram.thefreestuffbot.structures.Currency.USD;
import static com.github.tudeteam.telegram.thefreestuffbot.structures.UntilFormat.DATE;

public class SettingsMenu {

    protected final ConfigurationDB db;
    protected final SilentExecutor silent;

    public SettingsMenu(TheFreeStuffBot bot) {
        this.db = bot.configurationDB;
        this.silent = bot.silent;
    }

    protected static InlineKeyboardButton newCallbackButton(String text, String callbackData) {
        return new InlineKeyboardButton().setText(text).setCallbackData(callbackData);
    }

    protected InlineKeyboardMarkup constructSettingsMenuMarkup(long chatId) {
        InlineKeyboardMarkup menuMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = menuMarkup.getKeyboard();

        if (db.exists(chatId) && db.isAnnouncementsEnabled(chatId)) {
            ChatConfiguration config = db.getConfiguration(chatId);
            keyboard.add(SettingsButton.DISABLE_ANNOUNCEMENTS.buttonRow);
            keyboard.add((config.currency == USD ? SettingsButton.SET_CURRENCY_EUR : SettingsButton.SET_CURRENCY_USD).buttonRow);
            keyboard.add((config.untilFormat == DATE ? SettingsButton.SET_UNTIL_WEEKDAY : SettingsButton.SET_UNTIL_DATE).buttonRow);
            keyboard.add((config.trash ? SettingsButton.DISABLE_TRASH_FILTER : SettingsButton.ENABLE_TRASH_FILTER).buttonRow);
            keyboard.add(SettingsButton.SET_MINIMUM_PRICE.formattedRow(config.currency.symbol(), config.minPrice));
            keyboard.add(List.of(SettingsButton.SUPPORT_BOT.button, SettingsButton.CONFIGURATION_HELP.button));
        } else {
            keyboard.add(SettingsButton.ENABLE_ANNOUNCEMENTS.buttonRow);
            if (db.exists(chatId)) keyboard.add(SettingsButton.DELETE_CONFIGURATION.buttonRow);
        }

        return menuMarkup;
    }

    protected enum SettingsButton {
        ENABLE_ANNOUNCEMENTS("Enable announcements 📢", "menu:announcements-enable"),
        DISABLE_ANNOUNCEMENTS("Disable announcements ⚠", "menu:announcements-disable"),
        DELETE_CONFIGURATION("Delete configuration ♻", "menu:configuration-delete"),
        SET_CURRENCY_USD("Currency used: € - switch to $", "menu:currency-set-usd"),
        SET_CURRENCY_EUR("Currency used: $ - switch to €", "menu:currency-set-eur"),
        SET_UNTIL_DATE("Until format: weekday - switch to date", "menu:until-set-date"),
        SET_UNTIL_WEEKDAY("Until format: date - switch to weekday", "menu:until-set-weekday"),
        ENABLE_TRASH_FILTER("Filter low-quality games: ❎ - toggle", "menu:trash-enable"),
        DISABLE_TRASH_FILTER("Filter low-quality games: ✅ - toggle", "menu:trash-disable"),
        SET_MINIMUM_PRICE("Minimal original price: %c%04.2d - change", "menu:min-price-set"),
        SUPPORT_BOT("Support bot ♥", "menu:support-bot"),
        CONFIGURATION_HELP("Configuration help ℹ", "menu:help");

        public final InlineKeyboardButton button;
        public final List<InlineKeyboardButton> buttonRow;

        SettingsButton(String text, String callbackData) {
            button = newCallbackButton(text, callbackData);
            buttonRow = List.of(button);
        }

        public InlineKeyboardButton formatted(Object... args) {
            return new InlineKeyboardButton()
                    .setText(String.format(button.getText(), args))
                    .setCallbackData(button.getCallbackData());
        }

        public List<InlineKeyboardButton> formattedRow(Object... args) {
            return List.of(formatted(args));
        }

        @Override
        public String toString() {
            return button.getCallbackData();
        }
    }
}
