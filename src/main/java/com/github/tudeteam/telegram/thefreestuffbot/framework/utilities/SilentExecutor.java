package com.github.tudeteam.telegram.thefreestuffbot.framework.utilities;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.stickers.AddStickerToSet;
import org.telegram.telegrambots.meta.api.methods.stickers.CreateNewStickerSet;
import org.telegram.telegrambots.meta.api.methods.stickers.SetStickerSetThumb;
import org.telegram.telegrambots.meta.api.methods.stickers.UploadStickerFile;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unused")
public class SilentExecutor {

    public final AbsSender bot;

    public SilentExecutor(AbsSender bot) {
        this.bot = bot;
    }

    public SilentMessageBuilder compose() {
        return new SilentMessageBuilder();
    }

    public <T extends Serializable, Method extends BotApiMethod<T>, Callback extends SentCallback<T>> void executeAsync(Method method, Callback callback) {
        try {
            bot.executeAsync(method, callback);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) {
        try {
            return bot.execute(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Message execute(SendDocument sendDocument) {
        try {
            return bot.execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Message execute(SendPhoto sendPhoto) {
        try {
            return bot.execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Message execute(SendVideo sendVideo) {
        try {
            return bot.execute(sendVideo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Message execute(SendVideoNote sendVideoNote) {
        try {
            return bot.execute(sendVideoNote);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Message execute(SendSticker sendSticker) {
        try {
            return bot.execute(sendSticker);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Message execute(SendAudio sendAudio) {
        try {
            return bot.execute(sendAudio);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Message execute(SendVoice sendVoice) {
        try {
            return bot.execute(sendVoice);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Message> execute(SendMediaGroup sendMediaGroup) {
        try {
            return bot.execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean execute(SetChatPhoto setChatPhoto) {
        try {
            return bot.execute(setChatPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean execute(AddStickerToSet addStickerToSet) {
        try {
            return bot.execute(addStickerToSet);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean execute(SetStickerSetThumb setStickerSetThumb) {
        try {
            return bot.execute(setStickerSetThumb);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean execute(CreateNewStickerSet createNewStickerSet) {
        try {
            return bot.execute(createNewStickerSet);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public File execute(UploadStickerFile uploadStickerFile) {
        try {
            return bot.execute(uploadStickerFile);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Serializable execute(EditMessageMedia editMessageMedia) {
        try {
            return bot.execute(editMessageMedia);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Message execute(SendAnimation sendAnimation) {
        try {
            return bot.execute(sendAnimation);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Composes bot's messages with a much sweeter syntax.
     * And handles the exceptions silently.
     */
    public class SilentMessageBuilder extends MessageBuilder {
        public SilentMessageBuilder() {
            super(bot);
        }

        /**
         * Sends the composed message.
         *
         * @return The sent message on success, {@code null} otherwise.
         */
        @Override
        public Message send() {
            try {
                return super.send();
            } catch (TelegramApiException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
