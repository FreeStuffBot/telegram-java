package com.github.tudeteam.telegram.thefreestuffbot.database_patch;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.type;
import static com.mongodb.client.model.Updates.set;
import static org.bson.BsonType.STRING;

public class ConvertUntilIntoTimestamp {
    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase mongoDatabase = mongoClient.getDatabase("freestuffbot");
        MongoCollection<Document> collection = mongoDatabase.getCollection("games");

        collection.find(type("info.until", STRING)).forEach(document -> {
            System.out.println("Updating " + document.get("_id"));

            int published = document.getInteger("published");

            Document info = (Document) document.get("info");
            int until = published + Integer.parseInt(info.getString("until")) * 3600 * 24;
            info.put("until", until);

            collection.updateOne(eq("_id", document.get("_id")), set("info", info));
        });

        System.out.println("Made all 'until' strings into timestamps.");
    }
}
