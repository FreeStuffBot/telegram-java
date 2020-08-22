package com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.commands.authorizers;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.BasicAuthorizer;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * An authorizer which looks up the access level from a MongoDB database.
 * <p>
 * Each admin is stored as a document with a single field: <i>_id</i> which is the user id as a number.
 */
public class AuthorizeWithMongoDB implements BasicAuthorizer {

    /**
     * The user id of the bot creator to authorize as owner.
     */
    protected final int creatorId;

    /**
     * A silent executor of the bot to check the group admin.
     */
    protected final SilentExecutor silent;

    /**
     * A mongoDB collection for storing the admins list.
     */
    protected final MongoCollection<Document> admins;

    /**
     * Creates an authorizer which uses a MongoDB database to lookup admins.
     *
     * @param creatorId The user id of the bot creator to authorize as owner.
     * @param silent    A silent executor of the bot to check the group admin.
     * @param admins    A mongoDB collection for storing the admins list.
     */
    public AuthorizeWithMongoDB(int creatorId, SilentExecutor silent, MongoCollection<Document> admins) {
        this.creatorId = creatorId;
        this.silent = silent;
        this.admins = admins;
    }

    @Override
    public boolean isAdmin(Message message) {
        return admins.find(Filters.eq("_id", message.getFrom().getId())).first() != null;
    }

    @Override
    public boolean isOwner(Message message) {
        return message.getFrom().getId() == creatorId;
    }

    @Override
    public boolean isGroupAdmin(Message message) {
        if (message.isUserMessage()) return true;
        ChatMember member = silent.execute(new GetChatMember()
                .setChatId(message.getChatId())
                .setUserId(message.getFrom().getId()));
        if (member == null) return false;
        return member.getStatus().equals("administrator") || member.getStatus().equals("creator");
    }
}
