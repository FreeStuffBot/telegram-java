package com.github.tudeteam.telegram.thefreestuffbot;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.Var;

import java.lang.reflect.Type;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class MongoDBContext implements DBContext {
    private static final Gson gson = new Gson();
    private final MongoClient client;
    private final MongoDatabase db;

    public MongoDBContext(MongoClient client, String dbName) {
        this.client = client;
        this.db = client.getDatabase(dbName);
    }

    @Override
    public <T> List<T> getList(String name) {
        return new ArrayList<>(); //TODO
    }

    @Override
    public <K, V> Map<K, V> getMap(String name) {
        MongoCollection<Document> collection = db.getCollection(name);

        return new Map<>() {

            @Override
            public int size() {
                return (int) collection.countDocuments();
            }

            @Override
            public boolean isEmpty() {
                return size() == 0;
            }

            @Override
            public boolean containsKey(Object key) {
                return collection.find(eq("_id", key)).first() != null;
            }

            @Override
            public boolean containsValue(Object value) {
                Document docValue = Document.parse(gson.toJson(value));
                return collection.find(eq("value", docValue)).first() != null;
            }

            @Override
            public V get(Object key) {
                Document doc = collection.find(eq("_id", key)).first();
                if (doc == null) return null;
                try {
                    return gson.fromJson(((Document) doc.get("value")).toJson(), (Type) Class.forName(doc.getString("type")));
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }

            @Override
            public V put(K key, V value) {
                Object docValue = value;
                if (!(value instanceof Number || value instanceof Boolean || value instanceof String))
                    docValue = Document.parse(gson.toJson(value));

                if (collection.find(eq("_id", key)).first() == null)
                    collection.insertOne(new Document("_id", key).append("value", docValue).append("type", value.getClass().getName()));
                else
                    collection.updateOne(eq("_id", key), combine(set("value", docValue), set("type", value.getClass().getName())));

                return value;
            }

            @Override
            public V remove(Object key) {
                Document doc = collection.find(eq("_id", key)).first();
                if (doc == null) return null;
                collection.deleteOne(doc);
                try {
                    return gson.fromJson(doc.toJson(), (Type) Class.forName(doc.getString("type")));
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }

            @Override
            public void putAll(@NotNull Map<? extends K, ? extends V> m) {
                List<Document> toInsert = new ArrayList<>();
                m.forEach((key, value) -> {
                    Document docValue = Document.parse(gson.toJson(value));
                    if (collection.find(eq("_id", key)).first() == null)
                        toInsert.add(new Document("_id", key).append("value", docValue).append("type", value.getClass().getName()));
                    else
                        collection.updateOne(eq("_id", key), set("value", docValue));
                });
                collection.insertMany(toInsert);
            }

            @Override
            public void clear() {
                collection.drop();
            }

            @NotNull
            @Override
            public Set<K> keySet() {
                Set<K> set = new HashSet<>();
                //noinspection unchecked
                collection.find().forEach(doc -> set.add((K) doc.get("_id")));
                return set;
            }

            @NotNull
            @Override
            public Collection<V> values() {
                Set<V> set = new HashSet<>();
                collection.find().forEach(doc -> {
                    try {
                        set.add(gson.fromJson(((Document) doc.get("value")).toJson(), (Type) Class.forName(doc.getString("type"))));
                    } catch (ClassNotFoundException ignored) {

                    }
                });
                return set;
            }

            @NotNull
            @Override
            public Set<Entry<K, V>> entrySet() {
                Set<Entry<K, V>> set = new HashSet<>();
                collection.find().forEach(doc -> set.add(new Entry<>() {
                    @Override
                    public K getKey() {
                        //noinspection unchecked
                        return (K) doc.get("_id");
                    }

                    @Override
                    public V getValue() {
                        try {
                            return gson.fromJson(((Document) doc.get("value")).toJson(), (Type) Class.forName(doc.getString("type")));
                        } catch (ClassNotFoundException e) {
                            return null;
                        }
                    }

                    @Override
                    public V setValue(V value) {
                        Document docValue = Document.parse(gson.toJson(value));
                        doc.put("value", docValue);
                        doc.put("type", value.getClass().getName());
                        collection.updateOne(eq("_id", doc.get("_id")),
                                combine(set("value", docValue),
                                        set("type", value.getClass().getName())));
                        return value;
                    }
                }));
                return set;
            }
        };
    }

    @Override
    public <T> Set<T> getSet(String name) {
        return new HashSet<>(); //TODO
    }

    @Override
    public <T> Var<T> getVar(String name) {
        return null; //TODO
    }

    @Override
    public String summary() {
        return null; //TODO
    }

    @Override
    public Object backup() {
        return null; //TODO
    }

    @Override
    public boolean recover(Object backup) {
        return false; //TODO
    }

    @Override
    public String info(String name) {
        return null; //TODO
    }

    @Override
    public void commit() {
        //TODO
    }

    @Override
    public void clear() {
        db.drop();
    }

    @Override
    public boolean contains(String name) {
        return false; //TODO
    }

    @Override
    public void close() {
        client.close();
    }
}
