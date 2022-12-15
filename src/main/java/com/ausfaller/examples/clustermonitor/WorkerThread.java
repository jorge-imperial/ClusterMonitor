package com.ausfaller.examples.clustermonitor;

import com.mongodb.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class WorkerThread extends Thread {

    private static Logger logger = LogManager.getLogger(WorkerThread.class);

    public static String generateRandomChars(String candidateChars, int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(candidateChars.charAt(random.nextInt(candidateChars
                    .length())));
        }

        return sb.toString();
    }

    private final MongoClient mongoClient;
    private int threadNumber = -1;

    WorkerThread(int threadNumber, MongoClient mongoClient) {
        this.threadNumber = threadNumber;
        this.mongoClient = mongoClient;
    }
    @Override
    public void run() {

        DB db = mongoClient.getDB("test");
        DBCollection collection = db.getCollection("stocks");

        // Sleep, connecting every second.
        while (true) {

            try {
                long count = collection.count();
                //logger.info("Thread Awake: Document count is {}",  count);

                // Find operations
                String symbol = generateRandomChars("abcdefghijklmnopqrstuvwxyz", 4);
                DBObject query = new BasicDBObject("company_symbol", symbol);
                DBObject doc = collection.findOne(query);
                //if (doc != null)   logger.info("Found {} : {}", symbol, doc);


                // Find and modify
                symbol = generateRandomChars("abcdefghijklmnopqrstuvwxyz", 4);
                query = new BasicDBObject("company_symbol", symbol);
                DBObject modified = collection.findAndModify(query,
                        new BasicDBObject("$set", new BasicDBObject("ping", 1)));
                //logger.info("modified: {}", modified);
            }
            catch (MongoSocketOpenException socketOpenException) {
                logger.error("Socket: {}", socketOpenException.getMessage());
            }
            catch (MongoNodeIsRecoveringException mongoNodeIsRecoveringException) {
                logger.error("Is recovering: {}", mongoNodeIsRecoveringException.getMessage());
            }
        }
    }
}
