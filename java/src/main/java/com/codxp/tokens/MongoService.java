package com.codxp.tokens;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;

/**
 * Centralises access to the MongoDB collection used for users and token data.
 */
public final class MongoService {
    private static final MongoClient CLIENT = MongoClients.create(env("MONGODB_URI", "mongodb://localhost:27017"));
    private static final MongoCollection<Document> USERS_COLLECTION = initCollection();

    private MongoService() {
    }

    private static MongoCollection<Document> initCollection() {
        String databaseName = env("MONGODB_DATABASE", "codxp_tokens");
        String collectionName = env("MONGODB_USERS_COLLECTION", "users");
        MongoDatabase database = CLIENT.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.createIndex(Indexes.ascending("username"), new IndexOptions().unique(true));
        return collection;
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    public static MongoCollection<Document> usersCollection() {
        return USERS_COLLECTION;
    }
}
