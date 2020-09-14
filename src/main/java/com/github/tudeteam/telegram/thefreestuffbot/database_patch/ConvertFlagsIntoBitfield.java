package com.github.tudeteam.telegram.thefreestuffbot.database_patch;

import com.github.tudeteam.telegram.thefreestuffbot.structures.GameFlag;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.type;
import static com.mongodb.client.model.Updates.set;
import static org.bson.BsonType.ARRAY;

public class ConvertFlagsIntoBitfield {
    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase mongoDatabase = mongoClient.getDatabase("freestuffbot");
        MongoCollection<Document> collection = mongoDatabase.getCollection("games");

        collection.find(type("info.flags", ARRAY)).forEach(document -> {
            int bits = 0;
            for (String flag : document.get("info", Document.class).getList("flags", String.class))
                bits |= GameFlag.valueOf(flag).bitmask();

            collection.updateOne(document, set("info.flags", bits));
        });

        System.out.println("Updated GameFlags to bitfield.");
    }
}
