package com.ausfaller.examples.clustermonitor;

import com.mongodb.connection.ServerConnectionState;
import com.mongodb.connection.ServerDescription;
import com.mongodb.event.ServerClosedEvent;
import com.mongodb.event.ServerDescriptionChangedEvent;
import com.mongodb.event.ServerListener;
import com.mongodb.event.ServerOpeningEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class MongoServerListener implements ServerListener {

    //@Inject(optional = true)
    //@Named("MONGO_SHUTDOWN_THRESHOLD_MILLIS")
    private static int MONGO_SHUTDOWN_THRESHOLD_MILLIS = 20_000;

    //@Inject
    //private static ShutdownManager shutdownManager;

    private static final Logger logger =  LogManager.getLogger(MongoServerListener.class);
    private final String connectionName;
    private final AtomicLong failCount = new AtomicLong(0);

    public MongoServerListener(String connectionName) {
        this.connectionName = connectionName;
    }

    @Override
    public void serverDescriptionChanged(ServerDescriptionChangedEvent event) {
        final boolean connected = event.getNewDescription().getState() == ServerConnectionState.CONNECTED;
        if (connected) {
            long failTime = failCount.getAndSet(0);
            if (failTime != 0) {
                // reports: CONNECTED on a mongos that has not really
                logger.info("Recovered mongo connection {}", connectionName);
            }
            ServerDescription newDescription = event.getNewDescription();
            logger.info("connected isOk:{}. state: {}", newDescription.isOk(), newDescription.getState());
        } else {
            Date now = new Date();
            long failTime = failCount.updateAndGet(prev -> prev == 0 ? now.getTime() : prev);

            long serverDownMillis = now.getTime() - failTime;
            if (serverDownMillis > MONGO_SHUTDOWN_THRESHOLD_MILLIS) {
                logger.error(
                        "Lost Mongo connection {} for {} milliseconds, exceeding threshold ({})! Exiting",
                        connectionName,
                        serverDownMillis,
                        MONGO_SHUTDOWN_THRESHOLD_MILLIS
                );
                logger.error( "Calling shutdownManager.shutDown(1)");
            } else {
                logger.warn(
                        "Lost Mongo connection {} for {} milliseconds. Trying to reconnect",
                        connectionName,
                        serverDownMillis
                );
            }
        }
        logger.info("Server event {}", event.getNewDescription().getShortDescription());
    }

    @Override
    public void serverOpening(ServerOpeningEvent event) {
        logger.info("Server opening {}", event.getServerId());
    }

    @Override
    public void serverClosed(ServerClosedEvent event) {
        logger.info("Server closing {}", event.getServerId());
    }
}
