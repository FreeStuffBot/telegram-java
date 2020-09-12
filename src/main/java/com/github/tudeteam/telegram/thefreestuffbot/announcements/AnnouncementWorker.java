package com.github.tudeteam.telegram.thefreestuffbot.announcements;

import com.github.tudeteam.telegram.thefreestuffbot.ConfigurationDB;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.ChatUtilities;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import com.github.tudeteam.telegram.thefreestuffbot.structures.ChatConfiguration;
import com.github.tudeteam.telegram.thefreestuffbot.structures.GameInfo;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

public abstract class AnnouncementWorker implements Runnable {

    /**
     * The maximum number of announcements processed in each second.
     */
    public static final int rateLimit = 5;
    private static final Gson gson = new Gson();
    private static final Message nullMessage = new Message();
    /**
     * The silent executor used for executing the Telegram requests.
     */
    protected final SilentExecutor silent;

    /**
     * The chats configuration database.
     */
    protected final ConfigurationDB db;

    /**
     * The MongoDB collection containing the ongoing (under processing) announcements.
     */
    protected final MongoCollection<Document> ongoing;

    /**
     * The announcement to process.
     */
    protected final ObjectId announcementId;

    /**
     * The type of the announcement.
     */
    protected final String announcementType;

    /**
     * The data of the announcement.
     */
    protected final Document announcementData;

    /**
     * The timestamp we're measuring the rate second to (in milliseconds).
     */
    private long rateTimestamp;

    /**
     * The number of announcements processed in this second.
     */
    private int currentRate;

    public AnnouncementWorker(SilentExecutor silent, ConfigurationDB db, MongoCollection<Document> ongoing, ObjectId announcementId, String announcementType, Document announcementData) {
        this.silent = silent;
        this.db = db;
        this.ongoing = ongoing;
        this.announcementId = announcementId;
        this.announcementType = announcementType;
        this.announcementData = announcementData;
    }

    /**
     * When the worker asks for the next chat to announce in, and finds it has reached the end.
     */
    protected abstract void finishedQueue();

    /**
     * Pops the next chatId from the database and returns it, returns null when no chatId is available (reached the end).
     *
     * @return The next chatId, or {@code null} when the end is reached.
     */
    protected Long popChatId() {
        Document document = ongoing.findOneAndUpdate(eq("_id", announcementId), popFirst("chats.pending"));
        if (document == null) return null;
        List<Long> chats = document.get("chats", Document.class).getList("pending", Long.class);
        if (chats.isEmpty()) return null;
        return chats.get(0);
    }

    /**
     * Attempt to increase the current rate counter, if the limit is reached, it'll sleep until it resets.
     *
     * @throws InterruptedException If interrupted while waiting for the rate limit to reset.
     */
    protected void consumeRateLimit() throws InterruptedException {
        long timestamp = System.currentTimeMillis();
        if (timestamp - rateTimestamp >= 1000L) {
            rateTimestamp = timestamp;
            currentRate = 0;
        }

        if (currentRate >= rateLimit) {
            Thread.sleep(1010L - (timestamp - rateTimestamp)); //The extra 10 ms is because Thread.sleep is not perfectly accurate.
            consumeRateLimit();
            return;
        }

        currentRate++;
    }

    /**
     * Send the announcement for the specific chat.
     *
     * @param chatId The chat to send the announcement to.
     * @return The sent message, or null on failure.
     */
    protected Message sendAnnouncement(long chatId) {
        if (announcementType.equals("TEST")) {
            String content = announcementData.getString("content");
            return silent.compose().text(String.valueOf(content))
                    .chatId(chatId).send();
        } else if (announcementType.equals("GAME")) {
            GameInfo gameInfo = gson.fromJson(announcementData.toJson(), GameInfo.class);

            ChatConfiguration configuration = db.getConfiguration(chatId);
            //Cancel the announcement if:
            //- The chat's configuration was deleted.
            if (configuration == null) return nullMessage;
            //- The chat's has the announcements disabled.
            if (!configuration.enabled) return nullMessage;
            //- It's a trash game announcement and the chat has them filtered.
            if (!configuration.trash && gameInfo.isTrash()) return nullMessage;
            //- The game's price is lower than the minimum price set for this channel.
            if (configuration.minPrice > gameInfo.price.inCurrency(configuration.currency)) return nullMessage;

            InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
            inlineMarkup.getKeyboard().add(List.of(new InlineKeyboardButton()
                    .setText("Get")
                    .setUrl(gameInfo.org_url.toString())
            ));

            return silent.execute(new SendPhoto()
                    .setChatId(chatId)
                    .setPhoto(gameInfo.thumbnail.toString())
                    .setCaption(String.format("<b>Free Game!</b>\n<b>%s</b>\n<s>%s</s> <b>Free</b> until %s • %s\nvia freestuffbot.xyz",
                            gameInfo.title,
                            gameInfo.org_price.toString(configuration.currency),
                            gameInfo.formatUntil(configuration.untilFormat),
                            gameInfo.store.toString()
                    ))
                    .setParseMode("HTML")
                    .setReplyMarkup(inlineMarkup)
            );
        } else {
            System.err.println("Unsupported announcement type '" + announcementType + "' for chat: " + chatId);
            return null;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                consumeRateLimit();
            } catch (InterruptedException e) {
                return; //Terminated while waiting for rate limit to reset.
            }

            Long chatId = popChatId();
            if (chatId == null) {
                finishedQueue();
                return;
            }

            Message message = sendAnnouncement(chatId);

            if (message == null)
                ongoing.updateOne(eq("_id", announcementId), push("chats.failed", chatId));
            else if (message == nullMessage) {
                ongoing.updateOne(eq("_id", announcementId),
                        push("chats.processed", chatId)
                );
            } else {
                String chatCategory = ChatUtilities.getChatType(message.getChat()).name().toLowerCase() + "s";
                ongoing.updateOne(eq("_id", announcementId), combine(
                        inc("reached." + chatCategory, 1),
                        push("chats.processed", chatId)
                ));
            }
        }
    }
}
