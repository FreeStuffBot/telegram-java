package com.github.tudeteam.telegram.thefreestuffbot.framework.utilities;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MessageBuilder {
    private final SendMessage sendMessage = new SendMessage();
    private final AbsSender bot;

    public MessageBuilder(AbsSender bot) {
        this.bot = bot;
    }

    public MessageBuilder text(String text) {
        sendMessage.setText(text);
        return this;
    }

    public MessageBuilder text(Object text) {
        sendMessage.setText(text.toString());
        return this;
    }

    public MessageBuilder raw() {
        sendMessage.setParseMode(null);
        return this;
    }

    public MessageBuilder raw(String text) {
        sendMessage.setText(text);
        sendMessage.setParseMode(null);
        return this;
    }

    public MessageBuilder markdown() {
        sendMessage.enableMarkdownV2(true);
        return this;
    }

    public MessageBuilder markdown(boolean enabled) {
        sendMessage.enableMarkdownV2(enabled);
        return this;
    }

    public MessageBuilder markdown(String text) {
        sendMessage.setText(text);
        sendMessage.enableMarkdownV2(true);
        return this;
    }

    public MessageBuilder html() {
        sendMessage.enableHtml(true);
        return this;
    }

    public MessageBuilder html(boolean enabled) {
        sendMessage.enableHtml(enabled);
        return this;
    }

    public MessageBuilder html(String text) {
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        return this;
    }

    public MessageBuilder parseMode(String parseMode) {
        sendMessage.setParseMode(parseMode);
        return this;
    }

    public MessageBuilder chatId(long chatId) {
        sendMessage.setChatId(chatId);
        return this;
    }

    public MessageBuilder chatId(String chatId) {
        sendMessage.setChatId(chatId);
        return this;
    }

    public MessageBuilder chatId(Chat chat) {
        sendMessage.setChatId(chat.getId());
        return this;
    }

    public MessageBuilder chatId(User user) {
        sendMessage.setChatId(user.getId().longValue());
        return this;
    }

    public MessageBuilder chatId(Message message) {
        sendMessage.setChatId(message.getChatId());
        return this;
    }

    public MessageBuilder replyTo(Integer messageId) {
        sendMessage.setReplyToMessageId(messageId);
        return this;
    }

    public MessageBuilder replyTo(Message message) {
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
        return this;
    }

    public MessageBuilder markup(ReplyKeyboard replyKeyboard) {
        sendMessage.setReplyMarkup(replyKeyboard);
        return this;
    }

    public MessageBuilder disableNotification() {
        sendMessage.disableNotification();
        return this;
    }

    public MessageBuilder enableNotification() {
        sendMessage.enableNotification();
        return this;
    }

    public MessageBuilder notification(boolean enabled) {
        if (enabled) sendMessage.enableNotification();
        else sendMessage.disableNotification();
        return this;
    }

    public MessageBuilder disableWebPagePreview() {
        sendMessage.disableWebPagePreview();
        return this;
    }

    public MessageBuilder enableWebPagePreview() {
        sendMessage.enableWebPagePreview();
        return this;
    }

    public MessageBuilder webPagePreview(boolean enabled) {
        if (enabled) sendMessage.enableWebPagePreview();
        else sendMessage.disableWebPagePreview();
        return this;
    }

    public Message send() throws TelegramApiException {
        return bot.execute(sendMessage);
    }
}