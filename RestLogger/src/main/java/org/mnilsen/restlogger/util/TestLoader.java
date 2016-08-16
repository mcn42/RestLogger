/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mnilsen.restlogger.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

/**
 *
 * @author michaeln
 */
public class TestLoader {

    private MongoClient client = null;
    private MongoDatabase database = null;
    private MongoCollection collection = null;

    public TestLoader() {
        this.client = new MongoClient();
        this.database = this.client.getDatabase("rest_logger");
        this.collection = this.database.getCollection("messages");
    }

    public void truncateMessages() {
        this.collection.drop();
    }

    public void loadMessages(int count) {
        try {
            String devId = UUID.randomUUID().toString();
            for (int i = 0; i < count; i++) {
                Document d = new Document();
                d.put("level", "INFO");
                d.put("deviceId", devId);
                d.put("message", String.format("Test log message number %s", i + 1));
                d.put("systemId", "test");
                d.put("subsystemId", "generator");
                d.put("timestamp", System.currentTimeMillis());
                System.out.println(String.format("Generated JSON: %s", d.toJson()));
                this.collection.insertOne(d);
            }

        } catch (Exception ex) {
            Logger.getLogger(TestLoader.class.getName()).log(Level.SEVERE, "Mongo insert error", ex);
        }
        System.out.println(String.format("Messages collection size is now %s", this.collection.count()));
    }

    public static void main(String[] args) {
        TestLoader tl = new TestLoader();
        tl.truncateMessages();
        tl.loadMessages(30);
    }
}
