package com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.commands;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Command;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Locality;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.ParsedCommand;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Privacy;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.mongodb.client.model.Filters.eq;

public class PromoteCommand extends Command {

    protected final MongoCollection<Document> admins;
    protected final SilentExecutor silent;
    protected final int creatorID;

    public PromoteCommand(MongoCollection<Document> admins, SilentExecutor silent, int creatorId) {
        this(admins, silent, creatorId, "promote", "Promote a user to be an admin of the bot.");
    }

    public PromoteCommand(MongoCollection<Document> admins, SilentExecutor silent, int creatorID, String name, String description) {
        super(name, description, Locality.USER, Privacy.ADMIN);
        this.admins = admins;
        this.silent = silent;
        this.creatorID = creatorID;
    }

    @Override
    public void action(Message message, ParsedCommand parsedCommand) {
        if (!message.isReply() || message.getReplyToMessage().getForwardFrom() == null) {
            silent.compose().markdown("Send this command as a reply to a __forwarded__ message from the user you wish to promote ℹ")
                    .replyToOnlyInGroup(message).send();
            return;
        }

        Message replyTo = message.getReplyToMessage();
        User toPromote = replyTo.getForwardFrom();

        if (toPromote.getBot()) {
            silent.compose().text("I won't trust that bot to administrate me 😒") //EASTER_EGG
                    .replyToOnlyInGroup(message).send();
            return;
        }

        if (toPromote.getId() == creatorID) {
            silent.compose().text("That's my owner 😊") //EASTER_EGG
                    .replyToOnlyInGroup(message).send();
            return;
        }

        if (admins.find(eq("_id", toPromote.getId())).first() != null) {
            silent.compose().text("The user is already an admin 🙃")
                    .replyToOnlyInGroup(message).send();
            return;
        }

        boolean success = admins.insertOne(new Document("_id", toPromote.getId())
                .append("promotedBy", message.getFrom().getId())
                .append("promotedAt", (int) (System.currentTimeMillis() / 1000)))
                .wasAcknowledged();

        silent.compose().text(success ? "Promoted to an admin successfully ✅" : "An error occurred while promoting ⚠")
                .replyToOnlyInGroup(message).send();
    }
}
