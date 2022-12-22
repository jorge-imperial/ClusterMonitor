package com.ausfaller.examples.clustermonitor;

import com.mongodb.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
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

        DBCollection threadCollection = db.getCollection("threads");

        // Sleep, connecting every second.
        while (true) {

            try {
                long count = collection.count();
                //logger.info("Thread Awake: Document count is {}",  count);

                // Find operations
                String symbol = generateRandomChars("abcdefghijklmnopqrstuvwxyz", 4);
                DBObject query = new BasicDBObject("company_symbol", symbol);
                DBObject doc = collection.findOne(query);
                if (doc != null)
                    logger.info("Worker {}. Found {} : {}", threadNumber, symbol, doc);
                else
                    logger.info("Worker {}: Not found.", threadNumber);


                Instant instant = Instant.now();

                // Find and modify
                symbol = generateRandomChars("abcdefghijklmnopqrstuvwxyz", 4);
                query = new BasicDBObject("company_symbol", symbol);
                DBObject modified = collection.findAndModify(query,
                        new BasicDBObject("$set",
                                new BasicDBObject("ping",  instant).append("worker", threadNumber)));
                logger.info("Worker {}: modified: {}", threadNumber, modified);

                // Insert
                WriteResult ins = threadCollection.insert(new BasicDBObject("worker", threadNumber).append("ts", Instant.now()));
                logger.info("Worker {}: {} ", threadNumber, ins);
            }
            catch (MongoSocketOpenException socketOpenException) {
                logger.error("Worker {} Socket: {}", threadNumber, socketOpenException.getMessage());
            }
            catch (MongoNodeIsRecoveringException mongoNodeIsRecoveringException) {
                logger.error("Worker {} Is recovering: {}", threadNumber, mongoNodeIsRecoveringException.getMessage());
            }
            catch (MongoClientException mongoClientException) {
                logger.error("Worker {} Exception: {}", threadNumber,  mongoClientException.getMessage());
            }
            catch (MongoInterruptedException mongoInterruptedException) {
                logger.error("Worker {} Exception: {}", threadNumber, mongoInterruptedException.getMessage());
            }
        }
    }
}
