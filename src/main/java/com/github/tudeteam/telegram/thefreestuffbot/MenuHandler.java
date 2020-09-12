package com.github.tudeteam.telegram.thefreestuffbot;

import com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.commands.authorizers.AuthorizeWithMongoDB;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.Handler;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import com.github.tudeteam.telegram.thefreestuffbot.structures.ChatConfiguration;
import com.github.tudeteam.telegram.thefreestuffbot.structures.Currency;
import com.github.tudeteam.telegram.thefreestuffbot.structures.UntilFormat;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class MenuHandler implements Handler<Update> {
    protected final SilentExecutor silent;
    protected final ConfigurationDB db;
    protected final AuthorizeWithMongoDB authorizer;

    public MenuHandler(SilentExecutor silent, ConfigurationDB db, AuthorizeWithMongoDB authorizer) {
        this.silent = silent;
        this.db = db;
        this.authorizer = authorizer;
    }

    public InlineKeyboardMarkup constructMenuKeyboard(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = keyboardMarkup.getKeyboard();

        if (db.exists(chatId) && db.isAnnouncementsEnabled(chatId)) {
            ChatConfiguration config = db.getConfiguration(chatId);

            keyboard.add(List.of(new InlineKeyboardButton()
                    .setText("Disable announcements ⚠")
                    .setCallbackData("menu:announcements-disable")
            ));

            if (config.currency == Currency.USD) {
                keyboard.add(List.of(new InlineKeyboardButton()
                        .setText("Currency used: $ - switch to €")
                        .setCallbackData("menu:currency-set-eur")
                ));
            } else {
                keyboard.add(List.of(new InlineKeyboardButton()
                        .setText("Currency used: € - switch to $")
                        .setCallbackData("menu:currency-set-usd")
                ));
            }

            if (config.untilFormat == UntilFormat.DATE) {
                keyboard.add(List.of(new InlineKeyboardButton()
                        .setText("Until format: date - switch to weekday")
                        .setCallbackData("menu:until-set-weekday")
                ));
            } else {
                keyboard.add(List.of(new InlineKeyboardButton()
                        .setText("Until format: weekday - switch to date")
                        .setCallbackData("menu:until-set-date")
                ));
            }

            keyboard.add(List.of(new InlineKeyboardButton()
                    .setText("Filter low-quality games: " + (config.trash ? "❎" : "✅") + " - toggle")
                    .setCallbackData("menu:trash-" + (config.trash ? "disable" : "enable"))
            ));

            keyboard.add(List.of(new InlineKeyboardButton()
                    .setText("Minimal original price: " + (config.currency == Currency.USD ? "$" : "€") + config.minPrice + " - change")
                    .setCallbackData("menu:min-price-set")
            ));

            keyboard.add(List.of(new InlineKeyboardButton()
                            .setText("Support bot ♥")
                            .setCallbackData("menu:support-bot")
                    , new InlineKeyboardButton()
                            .setText("Configuration help ℹ")
                            .setCallbackData("menu:help")
            ));

        } else {
            keyboard.add(List.of(new InlineKeyboardButton()
                    .setText("Enable announcements 📢")
                    .setCallbackData("menu:announcements-enable")
            ));

            if (db.exists(chatId))
                keyboard.add(List.of(new InlineKeyboardButton()
                        .setText("Delete configuration ♻")
                        .setCallbackData("menu:configuration-delete")
                ));
        }

        return keyboardMarkup;
    }

    protected void updateMenuKeyboard(Message message) {
        silent.execute(new EditMessageReplyMarkup()
                .setChatId(message.getChatId())
                .setMessageId(message.getMessageId())
                .setReplyMarkup(constructMenuKeyboard(message.getChatId())));
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

    protected boolean checkPermission(CallbackQuery query) {
        if (isAuthorized(query)) return true;
        silent.execute(new AnswerCallbackQuery()
                .setCallbackQueryId(query.getId())
                .setText("Only group administrators can change the bot's configuration ⚠")
        );
        return false;
    }

    @Override
    public boolean process(Update update) {
        if (!update.hasCallbackQuery()) return false;
        CallbackQuery query = update.getCallbackQuery();

        User from = query.getFrom();
        String data = query.getData();
        Message menuMessage = query.getMessage();

        if (from == null || data == null || menuMessage == null) return false;

        long chatId = menuMessage.getChatId();

        AnswerCallbackQuery response = new AnswerCallbackQuery();
        response.setCallbackQueryId(query.getId());

        switch (data) {
            case "menu:announcements-enable": {
                if (checkPermission(query)) {
                    if (db.exists(chatId))
                        db.setAnnouncements(chatId, true);
                    else
                        db.newConfiguration(chatId);

                    updateMenuKeyboard(menuMessage);
                    response.setText("New games announcements will be sent in this chat ✅");
                }
                break;
            }
            case "menu:announcements-disable": {
                if (checkPermission(query)) {
                    if (db.exists(chatId))
                        db.setAnnouncements(chatId, false);

                    response.setText("Games announcements will no longer be sent in this chat ✅");
                    updateMenuKeyboard(menuMessage);
                }
                break;
            }

            case "menu:currency-set-usd": {
                if (checkPermission(query)) {
                    if (db.exists(chatId))
                        db.setCurrency(chatId, Currency.USD);
                    else
                        response.setText("Games announcements are not enabled ⚠");

                    updateMenuKeyboard(menuMessage);
                }
                break;
            }
            case "menu:currency-set-eur": {
                if (checkPermission(query)) {
                    if (db.exists(chatId))
                        db.setCurrency(chatId, Currency.EUR);
                    else
                        response.setText("Games announcements are not enabled ⚠");

                    updateMenuKeyboard(menuMessage);
                }
                break;
            }

            case "menu:until-set-date": {
                if (checkPermission(query)) {
                    if (db.exists(chatId))
                        db.setUntilFormat(chatId, UntilFormat.DATE);
                    else
                        response.setText("Games announcements are not enabled ⚠");

                    updateMenuKeyboard(menuMessage);
                }
                break;
            }
            case "menu:until-set-weekday": {
                if (checkPermission(query)) {
                    if (db.exists(chatId))
                        db.setUntilFormat(chatId, UntilFormat.WEEKDAY);
                    else
                        response.setText("Games announcements are not enabled ⚠");

                    updateMenuKeyboard(menuMessage);
                }
                break;
            }

            case "menu:trash-enable": {
                if (checkPermission(query)) {
                    if (db.exists(chatId))
                        db.setTrash(chatId, true);
                    else
                        response.setText("Games announcements are not enabled ⚠");

                    updateMenuKeyboard(menuMessage);
                }
                break;
            }
            case "menu:trash-disable": {
                if (checkPermission(query)) {
                    if (db.exists(chatId))
                        db.setTrash(chatId, false);
                    else
                        response.setText("Games announcements are not enabled ⚠");

                    updateMenuKeyboard(menuMessage);
                }
                break;
            }

            case "menu:min-price-set": {
                if (checkPermission(query)) {
                    if (db.exists(chatId))
                        response.setText("WORK IN PROGRESS 🚧");
                    else
                        response.setText("Games announcements are not enabled ⚠");

                    updateMenuKeyboard(menuMessage);
                }
                break;
            }

            case "menu:configuration-delete": {
                if (checkPermission(query)) {
                    if (db.exists(chatId))
                        db.deleteConfiguration(chatId);

                    response.setText("Deleted the configuration successfully ✅");

                    updateMenuKeyboard(menuMessage);
                }
                break;
            }

            case "menu:support-bot": {
                response.setText("Thanks ♥\nBut we haven't implemented a way to support us yet 😅\nThe fact that you pressed this honors us 😊");
                response.setShowAlert(true);
                System.out.println("Someone pressed support-bot!!!");
                break;
            }

            case "menu:help": {
                response.setText("WORK IN PROGRESS 🚧");
                break;
            }

            default:
                return false;
        }

        silent.execute(response);

        return true;
    }
}
