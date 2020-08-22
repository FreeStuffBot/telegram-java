package com.github.tudeteam.telegram.thefreestuffbot;

import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.CommandsHandler;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Locality;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.Privacy;
import com.github.tudeteam.telegram.thefreestuffbot.framework.commands.authorizers.AuthorizeWithMongoDB;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.ConsumeOncePipe;
import com.github.tudeteam.telegram.thefreestuffbot.framework.pipes.Pipe;
import com.github.tudeteam.telegram.thefreestuffbot.framework.utilities.SilentExecutor;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class TheFreeStuffBot extends TelegramLongPollingBot {

    /* Configuration variables using Environment variables */
    protected static final String botToken = System.getenv("BOT_TOKEN");
    protected static final String botUsername = System.getenv("BOT_USERNAME");
    protected static final int botCreatorID = Integer.parseInt(System.getenv("BOT_CREATORID"));
    protected static final String botDatabaseURI = System.getenv("BOT_DATABASE");
    /* End of configuration */

    protected static final MongoClient mongoClient = MongoClients.create(botDatabaseURI);
    protected static final MongoDatabase mongoDatabase = mongoClient.getDatabase("freestuffbot");
    protected static final MongoCollection<Document> adminsCollection = mongoDatabase.getCollection("telegram-admins");

    protected final Pipe<Update> updatesPipe = new ConsumeOncePipe<>();
    protected final SilentExecutor silent = new SilentExecutor(this);
    protected final AuthorizeWithMongoDB authorizer = new AuthorizeWithMongoDB(botCreatorID, silent, adminsCollection);
    protected final CommandsHandler commandsHandler = new CommandsHandler(botUsername, silent, authorizer);

    public TheFreeStuffBot() {
        updatesPipe.registerHandler(commandsHandler);

        commandsHandler.newCommand()
                .name("ping")
                .description("Pong 🏓")
                .action((message, parsedCommand) -> silent.compose().text("Pong 🏓")
                        .replyToOnlyInGroup(message).send())
                .build();

        commandsHandler.newCommand()
                .name("promote")
                .description("Promote a user to be an admin of the bot.")
                .privacy(Privacy.ADMIN)
                .locality(Locality.USER)
                .action((message, parsedCommand) -> {
                    if (!message.isReply()) {
                        silent.compose().text("Send this command as a reply to a message from the user you wish to promote ℹ")
                                .replyToOnlyInGroup(message).send();
                        return;
                    }

                    Message replyTo = message.getReplyToMessage();
                    User toPromote = replyTo.getForwardFrom();
                    if (toPromote == null) toPromote = replyTo.getFrom();

                    if (toPromote.getBot()) {
                        silent.compose().text("I won't trust that bot to administrate me 😒")
                                .replyToOnlyInGroup(message).send();
                        return;
                    }

                    if (adminsCollection.find(eq("_id", toPromote.getId())).first() != null) {
                        silent.compose().text("The user is already an admin 🙃")
                                .replyToOnlyInGroup(message).send();
                        return;
                    }

                    boolean success = adminsCollection.insertOne(new Document("_id", toPromote.getId())
                            .append("promotedBy", message.getFrom().getId())
                            .append("promotedAt", (int) (System.currentTimeMillis() / 1000)))
                            .wasAcknowledged();

                    silent.compose().text(success ? "Promoted to an admin successfully ✅" : "An error occurred while promoting ⚠")
                            .replyToOnlyInGroup(message).send();

                }).build();

        commandsHandler.newCommand()
                .name("demote")
                .description("Demote a bot administrator.")
                .privacy(Privacy.ADMIN)
                .locality(Locality.USER)
                .action((message, parsedCommand) -> {
                    if (!message.isReply()) {
                        silent.compose().text("Send this command as a reply to a message from the user you wish to demote ℹ")
                                .replyToOnlyInGroup(message).send();
                        return;
                    }

                    Message replyTo = message.getReplyToMessage();
                    User toDemote = replyTo.getForwardFrom();
                    if (toDemote == null) toDemote = replyTo.getFrom();

                    if (toDemote.getBot()) {
                        silent.compose().text("Heh, I already don't trust any bot to administrate me 😏")
                                .replyToOnlyInGroup(message).send();
                        return;
                    }

                    Document profile = adminsCollection.find(eq("_id", toDemote.getId())).first();
                    if (profile == null) {
                        silent.compose().text("The user is already not an admin to be demoted 😅")
                                .replyToOnlyInGroup(message).send();
                        return;
                    }

                    Integer promotedBy = profile.getInteger("promotedBy");
                    if (promotedBy == null && !authorizer.isOwner(message)) {
                        silent.compose().text("Only the bot's owner can demote this user ⚠")
                                .replyToOnlyInGroup(message).send();
                        return;
                    }

                    if (promotedBy != null && !promotedBy.equals(message.getFrom().getId()) && !authorizer.isOwner(message)) {
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

                    boolean success = adminsCollection.deleteOne(eq("_id", toDemote.getId())).wasAcknowledged();
                    if (!success) {
                        silent.compose().text("An error occurred while demoting ⚠")
                                .replyToOnlyInGroup(message).send();
                        return;
                    }

                    success = adminsCollection.updateMany(eq("promotedBy", toDemote.getId()),
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

                }).build();
    }

    /**
     * This method is called when receiving updates via GetUpdates method
     *
     * @param update Update received
     */
    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update);
        System.out.println(updatesPipe.process(update) ? "The update got consumed!" : "The update was not consumed!");
    }

    /**
     * Return bot username of this bot
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Returns the token of the bot to be able to perform Telegram Api Requests
     *
     * @return Token of the bot
     */
    @Override
    public String getBotToken() {
        return botToken;
    }
}
