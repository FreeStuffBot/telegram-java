package com.github.rami_sabbagh.telegram.alice_framework.utilities;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Composes bot's messages with a much sweeter syntax.
 */
@SuppressWarnings("unused")
public class MessageBuilder {
    private final SendMessage sendMessage = new SendMessage();
    private final AbsSender bot;

    /**
     * Composes a new message.
     *
     * @param bot The bot used for sending the message.
     */
    public MessageBuilder(AbsSender bot) {
        this.bot = bot;
    }

    /**
     * Sets the text of the message.
     *
     * @param text The text of the message.
     * @return this.
     */
    public MessageBuilder text(String text) {
        sendMessage.setText(text);
        return this;
    }

    /**
     * Sets the text of the message.
     *
     * @param text {@code toString()} will be used to convert into a String.
     * @return this.
     */
    public MessageBuilder text(Object text) {
        sendMessage.setText(text.toString());
        return this;
    }

    /**
     * Sets the text parse mode to raw (no format).
     *
     * @return this.
     */
    public MessageBuilder raw() {
        sendMessage.setParseMode(null);
        return this;
    }

    /**
     * Sets the text of the message, and sets the parsing mode to raw (no format).
     *
     * @param text The text of the message.
     * @return this.
     */
    public MessageBuilder raw(String text) {
        sendMessage.setText(text);
        sendMessage.setParseMode(null);
        return this;
    }

    /**
     * Sets the parse mode of the message to <i>MarkdownV2</i>.
     *
     * @return this.
     */
    public MessageBuilder markdown() {
        sendMessage.enableMarkdownV2(true);
        return this;
    }

    /**
     * Sets the parse mode of the message to <i>MarkdownV2</i> or to <i>raw</i>.
     *
     * @param enabled {@code true} for <i>MarkdownV2</i>, {@code false} for <i>raw</i>.
     * @return this.
     */
    public MessageBuilder markdown(boolean enabled) {
        sendMessage.enableMarkdownV2(enabled);
        return this;
    }

    /**
     * Sets the text of the message, and uses <i>MarkdownV2</i> to parse it.
     *
     * @param text The text of the message.
     * @return this.
     */
    public MessageBuilder markdown(String text) {
        sendMessage.setText(text);
        sendMessage.enableMarkdownV2(true);
        return this;
    }

    /**
     * Sets the parse mode of the message to <i>html</i>.
     *
     * @return this.
     */
    public MessageBuilder html() {
        sendMessage.enableHtml(true);
        return this;
    }

    /**
     * Sets the parse mode of the message to <i>html</i> or to <i>raw</i>.
     *
     * @param enabled {@code true} for <i>html</i>, {@code false} for <i>raw</i>.
     * @return this.
     */
    public MessageBuilder html(boolean enabled) {
        sendMessage.enableHtml(enabled);
        return this;
    }

    /**
     * Sets the text of the message, and uses <i>html</i> to parse it.
     *
     * @param text The text of the message.
     * @return this.
     */
    public MessageBuilder html(String text) {
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        return this;
    }

    /**
     * Sets the parsing mode of the message.
     *
     * @param parseMode The parsing mode of the message, check Telegram's docs.
     * @return this.
     */
    public MessageBuilder parseMode(String parseMode) {
        sendMessage.setParseMode(parseMode);
        return this;
    }

    /**
     * Sets the chat to send the message in.
     *
     * @param chatId The id of the chat to send the message in.
     * @return this.
     */
    public MessageBuilder chatId(long chatId) {
        sendMessage.setChatId(chatId);
        return this;
    }

    /**
     * Sets the chat to send the message in.
     *
     * @param chatId The <i>@username</i> of the chat to send the message in (groups and channels only).
     * @return this.
     */
    public MessageBuilder chatId(String chatId) {
        sendMessage.setChatId(chatId);
        return this;
    }

    /**
     * Sets the chat to send the message in.
     *
     * @param chat The chat to send the message in.
     * @return this.
     */
    public MessageBuilder chatId(Chat chat) {
        sendMessage.setChatId(chat.getId());
        return this;
    }

    /**
     * Sets the chat to send the message in.
     *
     * @param user The user to send the message to using private messages.
     * @return this.
     */
    public MessageBuilder chatId(User user) {
        sendMessage.setChatId(user.getId().longValue());
        return this;
    }

    /**
     * Sets the chat to send the message in.
     *
     * @param message A message in the same chat we wish to send the message to.
     * @return this.
     */
    public MessageBuilder chatId(Message message) {
        sendMessage.setChatId(message.getChatId());
        return this;
    }

    /**
     * Sets the message to reply to.
     *
     * @param messageId The id of the message to reply to, can be null.
     * @return this.
     */
    public MessageBuilder replyTo(Integer messageId) {
        sendMessage.setReplyToMessageId(messageId);
        return this;
    }

    /**
     * Sets the message to reply to, and the chat to use.
     *
     * @param message The message to reply to, in the same chat to send to.
     * @return this.
     */
    public MessageBuilder replyTo(Message message) {
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
        return this;
    }

    /**
     * Sets the message to reply to only if it was a message in a group, and the chat to user.
     *
     * @param message The message to reply to if it was in a group, in the same chat to send to.
     * @return this.
     */
    public MessageBuilder replyToOnlyInGroup(Message message) {
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.isUserMessage() ? null : message.getMessageId());
        return this;
    }

    /**
     * Sets the reply markup of the message.
     *
     * @param replyKeyboard The reply markup of the message, can be null.
     * @return this.
     */
    public MessageBuilder markup(ReplyKeyboard replyKeyboard) {
        sendMessage.setReplyMarkup(replyKeyboard);
        return this;
    }

    /**
     * Sets the message to be sent silently.
     *
     * @return this.
     */
    public MessageBuilder disableNotification() {
        sendMessage.disableNotification();
        return this;
    }

    /**
     * Sets tne message to be sent with notification (default).
     *
     * @return this.
     */
    public MessageBuilder enableNotification() {
        sendMessage.enableNotification();
        return this;
    }

    /**
     * Sets whether the message has to be sent silently or not.
     *
     * @param enabled {@code false} to send silently.
     * @return this.
     */
    public MessageBuilder notification(boolean enabled) {
        if (enabled) sendMessage.enableNotification();
        else sendMessage.disableNotification();
        return this;
    }

    /**
     * Disable the webpage preview of any link contained in the message.
     *
     * @return this.
     */
    public MessageBuilder disableWebPagePreview() {
        sendMessage.disableWebPagePreview();
        return this;
    }

    /**
     * Enables the webpage preview of any link contained in the message (default).
     *
     * @return this.
     */
    public MessageBuilder enableWebPagePreview() {
        sendMessage.enableWebPagePreview();
        return this;
    }

    /**
     * Sets whether the a webpage preview of any link contained in the message has to be displayed or not.
     *
     * @param enabled {@code false} to disable the webpage preview.
     * @return this.
     */
    public MessageBuilder webPagePreview(boolean enabled) {
        if (enabled) sendMessage.enableWebPagePreview();
        else sendMessage.disableWebPagePreview();
        return this;
    }

    /**
     * Sends the composed message.
     * <p>
     * Prints a stacktrace into {@code stderr} or failure.
     *
     * @return The sent message on success, {@code null} otherwise.
     */
    public Message send() {
        try {
            return this.execute();
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Executes the SendMessage operation, throwing exception on failure.
     * <p>
     * Prints a stacktrace into {@code stderr} or failure.
     *
     * @return The sent message on success, {@code null} otherwise.
     * @throws TelegramApiException on failure.
     */
    public Message execute() throws TelegramApiException {
        return bot.execute(sendMessage);
    }
}