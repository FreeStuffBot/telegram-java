package com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.commands.authorizers;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.AuthorizeOwner;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * A {@code StandardAuthorizer} which looks up the bot admins from a MongoDB database, and the bot owner from a constant userId.
 * <p>
 * Each admin is stored as a document with a single field: <i>_id</i> which is the user id as a number.
 */
public class AuthorizeWithMongoDB extends AuthorizeOwner {

    /**
     * A mongoDB collection for storing the admins list.
     */
    protected final MongoCollection<Document> admins;

    /**
     * Creates an authorizer which uses a MongoDB database to lookup admins.
     *
     * @param silent  The silent executor to use for requesting more information about the users.
     * @param ownerId The Telegram userId of the bot's owner.
     * @param admins  A mongoDB collection containing the admins list.
     */
    public AuthorizeWithMongoDB(SilentExecutor silent, int ownerId, MongoCollection<Document> admins) {
        super(silent, ownerId);
        this.admins = admins;
    }

    @Override
    public boolean isAdmin(Message message) {
        return admins.find(Filters.eq("_id", message.getFrom().getId())).first() != null;
    }
}
