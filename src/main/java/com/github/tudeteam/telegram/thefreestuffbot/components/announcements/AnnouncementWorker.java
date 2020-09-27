package com.github.tudeteam.telegram.thefreestuffbot.components.announcements;

import com.github.rami_sabbagh.telegram.alice_framework.utilities.ChatUtilities;
import com.github.rami_sabbagh.telegram.alice_framework.utilities.ChatUtilities.ChatType;
import com.github.rami_sabbagh.telegram.alice_framework.utilities.SilentExecutor;
import com.github.tudeteam.telegram.thefreestuffbot.components.ConfigurationDB;
import com.github.tudeteam.telegram.thefreestuffbot.structures.ChatConfiguration;
import com.github.tudeteam.telegram.thefreestuffbot.structures.GameInfo;
import io.lettuce.core.api.sync.RedisCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMembersCount;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static com.github.rami_sabbagh.telegram.alice_framework.utilities.ChatUtilities.ChatType.*;
import static com.github.tudeteam.telegram.thefreestuffbot.structures.GameFlag.TRASH;

public class AnnouncementWorker implements Runnable {

    /* Instance Fields */

    protected final long _id;
    protected final GameInfo gameInfo;
    protected final SilentExecutor silent;
    protected final ConfigurationDB db;
    protected final RateLimitBucket rateLimit;
    protected final RedisCommands<String, String> redisCommands;

    //Redis fields keys.

    protected final String keyPrefix;
    protected final String keyPending;
    protected final String keyFailed;
    protected final String keyAttempts;

    /* Constructor */

    public AnnouncementWorker(long _id, GameInfo gameInfo, SilentExecutor silent, ConfigurationDB db, RateLimitBucket rateLimit, RedisCommands<String, String> redisCommands) {
        this._id = _id;
        this.gameInfo = gameInfo;
        this.silent = silent;
        this.db = db;
        this.rateLimit = rateLimit;
        this.redisCommands = redisCommands;

        //Redis fields keys.
        keyPrefix = "TheFreeStuffBot:ongoing:" + _id + ":";
        keyPending = keyPrefix + "pending";
        keyFailed = keyPrefix + "failed";
        keyAttempts = keyPrefix + "attempts";
    }

    /* Instance Methods */

    protected void requeueFailed() {
        synchronized (rateLimit) {
            if (redisCommands.get(keyAttempts).equals("0")) return;
            if (redisCommands.scard(keyFailed) == 0) return;
            redisCommands.sunionstore(keyPending, keyPending, keyFailed);
            redisCommands.decr(keyAttempts);
        }
    }

    protected boolean shouldSkipAnnouncement(ChatConfiguration config) {
        //Skip the announcement if:
        //- The chat's configuration was deleted.
        if (config == null) return true;
        //- The chat's has the announcements disabled.
        if (!config.enabled) return true;
        //- It's a trash game announcement and the chat has them filtered.
        if (!config.trash && gameInfo.hasFlag(TRASH)) return true;
        //- The game's price is lower than the minimum price set for this channel.
        return config.minPrice > gameInfo.price.inCurrency(config.currency);
    }

    protected Message sendAnnouncement(long chatId, ChatConfiguration config) {
        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
        inlineMarkup.getKeyboard().add(List.of(new InlineKeyboardButton()
                        .setText("Share")
                        .setSwitchInlineQuery("game_id:" + this._id),

                new InlineKeyboardButton()
                        .setText("Get")
                        .setUrl(gameInfo.org_url.toString())
        ));

        return silent.execute(new SendPhoto()
                .setChatId(chatId)
                .setPhoto(gameInfo.thumbnail.toString())
                .setCaption(gameInfo.formatCaption(config))
                .setParseMode("HTML")
                .setReplyMarkup(inlineMarkup)
        );
    }

    @Override
    public void run() {
        while (true) {
            String chatIdString = redisCommands.spop(keyPending);
            if (chatIdString == null) {
                //Reached the end, requeue failed chats if that's possible.
                requeueFailed();
                //Check if some chats got requeued.
                if (redisCommands.scard(keyPending) != 0)
                    continue; //New chats, continue to the next iteration.
                else
                    break; //No more chats, terminate the worker.
            }

            //The chat to announce to.
            long chatId = Long.parseLong(chatIdString);

            //The configuration of the chat.
            ChatConfiguration config = db.getConfiguration(chatId);

            //Check if the announcement should be skipped for this chat.
            if (shouldSkipAnnouncement(config)) continue;

            //Consume a call from the rateLimit bucket.
            try {
                rateLimit.consume();
            } catch (InterruptedException e) {
                redisCommands.sadd(keyPending, chatIdString); //Requeue the chat id.
                return; //The worker has been terminated by interruption.
            }

            //Send the announcement.
            Message message = sendAnnouncement(chatId, config);

            //Check if the message failed
            if (message == null) //It failed.
                redisCommands.sadd(keyFailed, chatIdString); //Add the chat id to the failed set.
            else { //It was sent.
                ChatType chatType = ChatUtilities.getChatType(message.getChat());
                if (chatType == UNKNOWN) continue;

                //Increase the chats counter of that type.
                redisCommands.incr(keyPrefix + chatType.name().toLowerCase() + "s");

                //Increase the members counters for groups and channels.
                if (chatType == GROUP || chatType == SUPERGROUP) {
                    Integer count = silent.execute(new GetChatMembersCount().setChatId(chatId));
                    if (count != null) redisCommands.incrby(keyPrefix + "groupsUsers", count);
                } else if (chatType == CHANNEL) {
                    Integer count = silent.execute(new GetChatMembersCount().setChatId(chatId));
                    if (count != null) redisCommands.incrby(keyPrefix + "channelsUsers", count);
                }
            }

        }
    }
}
