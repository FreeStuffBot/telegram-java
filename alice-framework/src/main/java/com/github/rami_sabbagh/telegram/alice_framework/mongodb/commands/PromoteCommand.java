package com.github.rami_sabbagh.telegram.alice_framework.mongodb.commands;

import com.github.rami_sabbagh.telegram.alice_framework.commands.Command;
import com.github.rami_sabbagh.telegram.alice_framework.commands.Locality;
import com.github.rami_sabbagh.telegram.alice_framework.commands.ParsedCommand;
import com.github.rami_sabbagh.telegram.alice_framework.commands.Privacy;
import com.github.rami_sabbagh.telegram.alice_framework.utilities.SilentExecutor;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.objects.Chat;
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
        if ((!message.isReply() || message.getReplyToMessage().getForwardFrom() == null) && parsedCommand.parameters.isBlank()) {
            silent.compose().markdown("Send this command as a reply to a __forwarded__ message from the user you wish to promote ℹ\n" +
                    "Or send the user id in `" + parsedCommand + " [userId]` ℹ")
                    .replyToOnlyInGroup(message).send();
            return;
        }

        int toPromote; //The id of the user to promote.

        if (!parsedCommand.parameters.isBlank()) {
            try {
                toPromote = Integer.parseInt(parsedCommand.parameters);
                Chat toPromoteChat = silent.execute(new GetChat().setChatId((long) toPromote));
                if (toPromoteChat == null) {
                    silent.compose().markdown("Invalid `userId` ⚠")
                            .replyToOnlyInGroup(message).send();
                    return;
                }
            } catch (NumberFormatException e) {
                silent.compose().markdown("Invalid `userId` ⚠")
                        .replyToOnlyInGroup(message).send();
                return;
            }
        } else {
            Message replyTo = message.getReplyToMessage();
            User toPromoteUser = replyTo.getForwardFrom();

            if (toPromoteUser.getBot()) {
                silent.compose().text("I won't trust that bot to administrate me 😒") //EASTER_EGG
                        .replyToOnlyInGroup(message).send();
                return;
            }

            toPromote = toPromoteUser.getId();
        }

        if (toPromote == creatorID) {
            silent.compose().text("That's my owner 😊") //EASTER_EGG
                    .replyToOnlyInGroup(message).send();
            return;
        }

        if (admins.find(eq("_id", toPromote)).first() != null) {
            silent.compose().text("The user is already an admin 🙃")
                    .replyToOnlyInGroup(message).send();
            return;
        }

        boolean success = admins.insertOne(new Document("_id", toPromote)
                .append("promotedBy", message.getFrom().getId())
                .append("promotedAt", (int) (System.currentTimeMillis() / 1000)))
                .wasAcknowledged();

        silent.compose().text(success ? "Promoted to an admin successfully ✅" : "An error occurred while promoting ⚠")
                .replyToOnlyInGroup(message).send();
    }
}
