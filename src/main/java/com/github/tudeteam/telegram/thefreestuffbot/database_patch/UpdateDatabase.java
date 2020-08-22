package com.github.tudeteam.telegram.thefreestuffbot.database_patch;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class UpdateDatabase {
    static MongoCollection<Document> getGamesCollection(String connectString, String collectionName) {
        return MongoClients.create(connectString).getDatabase("freestuffbot").getCollection(collectionName);
    }

    static void cloneCollection(String name) {
        MongoCollection<Document> remote = getGamesCollection(System.getenv("remote"), name);
        MongoCollection<Document> local = getGamesCollection(System.getenv("local"), name);

        List<Document> games = new ArrayList<>();
        remote.find().forEach(games::add);

        local.drop();
        local.insertMany(games);

        System.out.println("Pulled database successfully!");
    }

    public static void main(String[] args) {
        cloneCollection("games");
        cloneCollection("users");
        cloneCollection("language");

        ConvertUntilIntoString.main(new String[0]);
        ConvertUntilIntoTimestamp.main(new String[0]);
    }
}
