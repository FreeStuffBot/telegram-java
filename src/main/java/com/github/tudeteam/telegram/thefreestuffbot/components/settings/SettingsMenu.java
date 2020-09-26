package com.github.tudeteam.telegram.thefreestuffbot.components.settings;

import com.github.rami_sabbagh.telegram.alice_framework.commands.authorizers.PrivacyAuthorizer;
import com.github.rami_sabbagh.telegram.alice_framework.interactivity.InteractivityHandler;
import com.github.rami_sabbagh.telegram.alice_framework.pipes.Handler;
import com.github.rami_sabbagh.telegram.alice_framework.utilities.SilentExecutor;
import com.github.tudeteam.telegram.thefreestuffbot.TheFreeStuffBot;
import com.github.tudeteam.telegram.thefreestuffbot.components.ConfigurationDB;
import com.github.tudeteam.telegram.thefreestuffbot.structures.ChatConfiguration;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tudeteam.telegram.thefreestuffbot.components.settings.SettingsMenu.SettingsButton.*;
import static com.github.tudeteam.telegram.thefreestuffbot.structures.Currency.EUR;
import static com.github.tudeteam.telegram.thefreestuffbot.structures.Currency.USD;
import static com.github.tudeteam.telegram.thefreestuffbot.structures.UntilFormat.DATE;
import static com.github.tudeteam.telegram.thefreestuffbot.structures.UntilFormat.WEEKDAY;

public class SettingsMenu implements Handler<Update> {

    protected final String minimumPricePromptId = "MinimumPricePrompt";
    protected final PrivacyAuthorizer authorizer;
    protected final ConfigurationDB db;
    protected final SilentExecutor silent;
    protected final InteractivityHandler interactivityHandler;

    public SettingsMenu(TheFreeStuffBot bot) {
        authorizer = bot.authorizer;
        db = bot.configurationDB;
        silent = bot.silent;
        interactivityHandler = bot.interactivityHandler;

        bot.interactivityHandler.registerListener(minimumPricePromptId, new MinimumPricePrompt(bot));
    }

    protected static InlineKeyboardButton newCallbackButton(String text, String callbackData) {
        return new InlineKeyboardButton().setText(text).setCallbackData(callbackData);
    }

    public InlineKeyboardMarkup constructSettingsMenuMarkup(long chatId) {
        InlineKeyboardMarkup menuMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = menuMarkup.getKeyboard();

        if (db.exists(chatId) && db.isAnnouncementsEnabled(chatId)) {
            ChatConfiguration config = db.getConfiguration(chatId);
            keyboard.add(DISABLE_ANNOUNCEMENTS.buttonRow);
            keyboard.add((config.currency == USD ? SET_CURRENCY_EUR : SET_CURRENCY_USD).buttonRow);
            keyboard.add((config.untilFormat == DATE ? SET_UNTIL_WEEKDAY : SET_UNTIL_DATE).buttonRow);
            keyboard.add((config.trash ? ENABLE_TRASH_FILTER : DISABLE_TRASH_FILTER).buttonRow);
            keyboard.add(SET_MINIMUM_PRICE.formattedRow(config.currency.symbol(), config.minPrice));
            keyboard.add(List.of(SUPPORT_BOT.button, CLOSE_MENU.button));
        } else {
            keyboard.add(ENABLE_ANNOUNCEMENTS.buttonRow);
            if (db.exists(chatId)) keyboard.add(DELETE_CONFIGURATION.buttonRow);
            keyboard.add(CLOSE_MENU.buttonRow);
        }

        return menuMarkup;
    }

    public void updateSettingsMenu(Message message) {
        silent.execute(new EditMessageReplyMarkup()
                .setChatId(message.getChatId())
                .setMessageId(message.getMessageId())
                .setReplyMarkup(constructSettingsMenuMarkup(message.getChatId())));
    }

    public void closeSettingsMenu(Message message) {
        silent.execute(new EditMessageText()
                .setText(message.getText() + " [closed]")
                .setChatId(message.getChatId())
                .setMessageId(message.getMessageId()));
    }

    /**
     * Checks if the requester is allowed to change the bot's configuration for this chat.
     *
     * @param query The callback query to check.
     * @return {@code true} if it was authorized.
     */
    protected boolean isAuthorized(CallbackQuery query) {
        //A fake message object to use the CommandsAuthorizer for authorizing the query :p
        Message fakeMessage = new Message() {
            @Override
            public User getFrom() {
                return query.getFrom();
            }

            @Override
            public Chat getChat() {
                return query.getMessage().getChat();
            }

            @Override
            public Long getChatId() {
                return query.getMessage().getChatId();
            }

            @Override
            public boolean isUserMessage() {
                return query.getMessage().isUserMessage();
            }
        };

        return authorizer.isGroupAdmin(fakeMessage) || authorizer.isAdmin(fakeMessage) || authorizer.isOwner(fakeMessage);
    }

    @Override
    public boolean process(Update update) {
        if (!update.hasCallbackQuery()) return false;
        CallbackQuery query = update.getCallbackQuery();

        String data = query.getData();
        if (data == null) return false;

        SettingsButton button = SettingsButton.valueByData(data);
        if (button == null) return false;

        AnswerCallbackQuery response = new AnswerCallbackQuery();
        response.setCallbackQueryId(query.getId());

        Message message = query.getMessage();
        long chatId = message.getChatId();

        if (button.adminOnly && !isAuthorized(query)) {
            response.setText("Only group administrators can change the bot's configuration ⚠");
            silent.execute(response);
            return true;
        }

        if (button.checkDB && !db.exists(chatId)) {
            response.setText("Games announcements are not enabled ⚠");
            silent.execute(response);
            return true;
        }

        switch (button) {
            case ENABLE_ANNOUNCEMENTS: {
                if (db.exists(chatId))
                    db.setAnnouncements(chatId, true);
                else
                    db.newConfiguration(chatId);

                response.setText("New games announcements will be sent in this chat ✅");
                break;
            }

            case DISABLE_ANNOUNCEMENTS: {
                db.setAnnouncements(chatId, false);
                response.setText("Games announcements will no longer be sent in this chat ✅");
                break;
            }

            case DELETE_CONFIGURATION: {
                if (db.exists(chatId)) db.deleteConfiguration(chatId);
                response.setText("Deleted the configuration successfully ✅");
                break;
            }

            case SET_CURRENCY_USD: {
                db.setCurrency(chatId, USD);
                break;
            }

            case SET_CURRENCY_EUR: {
                db.setCurrency(chatId, EUR);
                break;
            }

            case SET_UNTIL_DATE: {
                db.setUntilFormat(chatId, DATE);
                break;
            }

            case SET_UNTIL_WEEKDAY: {
                db.setUntilFormat(chatId, WEEKDAY);
                break;
            }

            case ENABLE_TRASH_FILTER: {
                db.setTrash(chatId, false);
                break;
            }

            case DISABLE_TRASH_FILTER: {
                db.setTrash(chatId, true);
                break;
            }

            case SET_MINIMUM_PRICE: {
                closeSettingsMenu(message);
                interactivityHandler.activateListener(chatId, minimumPricePromptId);
                break;
            }

            case SUPPORT_BOT: {
                response.setText("Thanks ♥\nBut we haven't implemented a way to support us yet 😅\nThe fact that you pressed this honors us 😊");
                response.setShowAlert(true);
                System.out.println("Someone pressed support-bot!!!");
                break;
            }

            case CLOSE_MENU: {
                closeSettingsMenu(message);
                response.setText("Closed the menu successfully ✅");
                break;
            }

            case RETURN_TO_SETTINGS: {
                silent.execute(new EditMessageReplyMarkup()
                        .setChatId(chatId).setMessageId(message.getMessageId()));

                silent.compose().text("Settings menu ⚙")
                        .chatId(chatId).markup(constructSettingsMenuMarkup(chatId)).send();
                break;
            }

            default: {
                response.setText("🚧 WORK IN PROGRESS 🚧");
            }
        }

        if (button.updateMenu) updateSettingsMenu(message);
        silent.execute(response);

        return true;
    }

    protected enum SettingsButton {
        ENABLE_ANNOUNCEMENTS("Enable announcements 📢", "menu:announcements-enable", false),
        DISABLE_ANNOUNCEMENTS("Disable announcements ⚠", "menu:announcements-disable"),
        DELETE_CONFIGURATION("Delete configuration ♻", "menu:configuration-delete", false),
        SET_CURRENCY_USD("Currency used: € - switch to $", "menu:currency-set-usd"),
        SET_CURRENCY_EUR("Currency used: $ - switch to €", "menu:currency-set-eur"),
        SET_UNTIL_DATE("Until format: weekday - switch to date", "menu:until-set-date"),
        SET_UNTIL_WEEKDAY("Until format: date - switch to weekday", "menu:until-set-weekday"),
        ENABLE_TRASH_FILTER("Filter low-quality games: ❎ - toggle", "menu:trash-enable"),
        DISABLE_TRASH_FILTER("Filter low-quality games: ✅ - toggle", "menu:trash-disable"),
        SET_MINIMUM_PRICE("Minimal original price: %s%04.2f - change", "menu:min-price-set", false, false),
        SUPPORT_BOT("Support bot ♥", "menu:support-bot", false, false, false),
        CLOSE_MENU("Close menu ⚙", "menu:close", false, false),
        RETURN_TO_SETTINGS("Return to settings menu ⚙", "menu:settings", false, false);

        private static final Map<String, SettingsButton> byData = new HashMap<>();

        static {
            for (SettingsButton button : values())
                byData.put(button.button.getCallbackData(), button);
        }

        public final InlineKeyboardButton button;
        public final List<InlineKeyboardButton> buttonRow;
        public final boolean checkDB;
        public final boolean updateMenu;
        public final boolean adminOnly;

        SettingsButton(String text, String callbackData) {
            this(text, callbackData, true, true, true);
        }

        SettingsButton(String text, String callbackData, boolean checkDB) {
            this(text, callbackData, checkDB, true);
        }

        SettingsButton(String text, String callbackData, boolean checkDB, boolean updateMenu) {
            this(text, callbackData, checkDB, updateMenu, true);
        }

        SettingsButton(String text, String callbackData, boolean checkDB, boolean updateMenu, boolean adminOnly) {
            button = newCallbackButton(text, callbackData);
            buttonRow = List.of(button);
            this.checkDB = checkDB;
            this.updateMenu = updateMenu;
            this.adminOnly = adminOnly;
        }

        public static SettingsButton valueByData(String data) {
            return byData.get(data);
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
