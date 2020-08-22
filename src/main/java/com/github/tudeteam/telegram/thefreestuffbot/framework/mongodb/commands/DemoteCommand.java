package com.github.tudeteam.telegram.thefreestuffbot.framework.mongodb.commands;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Command;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Locality;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.ParsedCommand;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Privacy;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class DemoteCommand extends Command {

    protected final MongoCollection<Document> admins;
    protected final SilentExecutor silent;
    protected final int creatorID;

    public DemoteCommand(MongoCollection<Document> admins, SilentExecutor silent, int creatorId) {
        this(admins, silent, creatorId, "demote", "Demote a bot administrator.");
    }

    public DemoteCommand(MongoCollection<Document> admins, SilentExecutor silent, int creatorID, String name, String description) {
        super(name, description, Locality.USER, Privacy.ADMIN);
        this.admins = admins;
        this.silent = silent;
        this.creatorID = creatorID;
    }

    /**
     * Checks if a message is from the owner of the bot.
     *
     * @param message The message to check.
     * @return {@code true} if the message was from the owner.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isOwner(Message message) {
        return message.getFrom().getId() == creatorID;
    }

    @Override
    public void action(Message message, ParsedCommand parsedCommand) {

        if (!message.isReply() || message.getReplyToMessage().getForwardFrom() == null) {
            silent.compose().markdown("Send this command as a reply to a __forwarded__ message from the user you wish to demote ℹ")
                    .replyToOnlyInGroup(message).send();
            return;
        }

        Message replyTo = message.getReplyToMessage();
        User toDemote = replyTo.getForwardFrom();

        if (toDemote.getBot()) {
            silent.compose().text("Heh, I already don't trust any bot to administrate me 😏") //EASTER_EGG
                    .replyToOnlyInGroup(message).send();
            return;
        }

        if (toDemote.getId() == creatorID) {
            silent.compose().text("I won't ever demote my owner ❕") //EASTER_EGG
                    .replyToOnlyInGroup(message).send();
            return;
        }

        Document profile = admins.find(eq("_id", toDemote.getId())).first();
        if (profile == null) {
            silent.compose().text("The user is already not an admin to be demoted 😅")
                    .replyToOnlyInGroup(message).send();
            return;
        }

        Integer promotedBy = profile.getInteger("promotedBy");
        if (promotedBy == null && !isOwner(message)) {
            silent.compose().text("Only the bot's owner can demote this user ⚠")
                    .replyToOnlyInGroup(message).send();
            return;
        }

        if (promotedBy != null && !promotedBy.equals(message.getFrom().getId()) && !isOwner(message)) {
            String bossName;
            Chat bossChat = silent.execute(new GetChat().setChatId(message.getFrom().getId().longValue()));
            if (bossChat == null)
                bossName = "{User #" + message.getFrom().getId() + "}";
            else if (bossChat.getUserName() != null)
                bossName = "@" + bossChat.getUserName();
            else {
                bossName = bossChat.getFirstName();
                if (bossChat.getLastName() != null) bossName += " " + bossChat.getLastName();
                bossName += " #" + message.getFrom().getId();
            }

            silent.compose().text("This user can be only demoted by " + bossName + " or by the owner ⚠")
                    .replyToOnlyInGroup(message).send();
            return;
        }

        boolean success = admins.deleteOne(eq("_id", toDemote.getId())).wasAcknowledged();
        if (!success) {
            silent.compose().text("An error occurred while demoting ⚠")
                    .replyToOnlyInGroup(message).send();
            return;
        }

        success = admins.updateMany(eq("promotedBy", toDemote.getId()),
                set("promotedBy", message.getFrom().getId())).wasAcknowledged();

        if (!success) {
            System.err.println("An error occurred while transferring sub-admins from " + toDemote.getId()
                    + " to " + message.getFrom().getId());

            silent.compose().text("An error occurred while transferring sub-admins ⚠")
                    .replyToOnlyInGroup(message).send();
            return;
        }

        silent.compose().text("Demoted successfully ✅")
                .replyToOnlyInGroup(message).send();

    }
}
