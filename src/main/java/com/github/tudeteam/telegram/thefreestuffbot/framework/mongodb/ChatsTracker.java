package com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb;

import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.Handler;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;

public class ChatsTracker implements Handler<Update> {

    protected final String botUsername;
    protected final MongoCollection<Document> chats;

    public ChatsTracker(String botUsername, MongoCollection<Document> chats) {
        this.botUsername = botUsername;
        this.chats = chats;
    }

    protected String getChatType(Chat chat) {
        if (chat.isUserChat()) return "private";
        else if (chat.isGroupChat()) return "group";
        else if (chat.isSuperGroupChat()) return "supergroup";
        else if (chat.isChannelChat()) return "channel";
        else return "unknown";
    }

    protected boolean isNewChat(Chat chat) {
        return chats.find(eq("_id", chat.getId())).first() == null;
    }

    protected void recordChat(Chat chat) {
        if (!isNewChat(chat)) return;
        chats.insertOne(new Document("_id", chat.getId())
                .append("type", getChatType(chat))
                .append("discoveredAt", (int) (System.currentTimeMillis() / 1000L)));
    }

    protected void migrateChat(Message message) {
        Document document = chats.find(eq("_id", message.getMigrateFromChatId())).first();

        if (document == null) {
            recordChat(message.getChat());
            return;
        }

        document.put("_id", message.getMigrateToChatId());

        chats.insertOne(document);
        chats.deleteOne(eq("_id", message.getMigrateFromChatId()));
    }

    protected void removeChat(Chat chat) {
        chats.deleteOne(eq("_id", chat.getId()));
    }

    @Override
    public boolean process(Update update) {
        if (!update.hasMessage()) return false;

        Message message = update.getMessage();

        if (message.isUserMessage()) {
            if (message.getText().equals("/start"))
                recordChat(message.getChat());
        } else {
            if (message.getGroupchatCreated() != null && message.getGroupchatCreated())
                recordChat(message.getChat());
            else if (message.getNewChatMembers() != null) {
                for (User user : message.getNewChatMembers()) {
                    if (Objects.equals(user.getUserName(), botUsername)) {
                        recordChat(message.getChat());
                        break;
                    }
                }
            } else if (message.getLeftChatMember() != null) {
                if (Objects.equals(message.getLeftChatMember().getUserName(), botUsername))
                    removeChat(message.getChat());
            } else if (message.getMigrateFromChatId() != null) {
                migrateChat(message);
            }
        }

        return false;
    }
}
