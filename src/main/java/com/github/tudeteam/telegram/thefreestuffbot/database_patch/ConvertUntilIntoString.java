package com.github.tudeteam.telegram.thefreestuffbot.database_patch;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.type;
import static com.mongodb.client.model.Updates.set;
import static org.bson.BsonType.INT32;

public class ConvertUntilIntoString {
    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase mongoDatabase = mongoClient.getDatabase("freestuffbot");
        MongoCollection<Document> collection = mongoDatabase.getCollection("games");

        collection.find(type("info.until", INT32)).forEach(document -> {
            System.out.println("Updating " + document.get("_id"));
            Document info = (Document) document.get("info");
            info.put("until", info.getInteger("until").toString());

            collection.updateOne(eq("_id", document.get("_id")), set("info", info));
        });

        System.out.println("Made all 'until' timestamps into Strings.");
    }
}
