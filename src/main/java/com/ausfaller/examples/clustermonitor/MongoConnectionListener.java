package com.ausfaller.examples.clustermonitor;

import com.mongodb.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MongoConnectionListener implements ConnectionPoolListener {


    /**
     * Invoked when a connection pool is created. The default implementation does nothing.
     */
    private static final Logger logger =  LogManager.getLogger(MongoConnectionListener.class);
    public void connectionPoolCreated(ConnectionPoolCreatedEvent event) {
        logger.info("Pool created: {}", event.getServerId());
    }

    /**
     * Invoked when a connection pool is cleared and paused. The default implementation does nothing.
     */
    public void connectionPoolCleared(ConnectionPoolClearedEvent event) {
        logger.info("Pool cleared: {}", event.getServerId());
    }

    /**
     * Invoked when a connection pool is ready. The default implementation does nothing.
     */
    public void connectionPoolReady(ConnectionPoolReadyEvent event) {
        logger.info("Pool ready: {}", event.getServerId());
    }

    /**
     * Invoked when a connection pool is closed. The default implementation does nothing.
     */
    public void connectionPoolClosed(ConnectionPoolClosedEvent event) {
        logger.info("Pool closed: {}", event.getServerId());
    }

    /**
     * Invoked when attempting to check out a connection from a pool. The default implementation does nothing.
     */
    public void connectionCheckOutStarted(ConnectionCheckOutStartedEvent event) {
        //logger.info("Check out start: {}", event.getServerId());
    }

    /**
     * Invoked when a connection is checked out of a pool. The default implementation does nothing.
     */
    public void connectionCheckedOut(ConnectionCheckedOutEvent event) {
        //logger.info("Check out: {}", event.getConnectionId());
    }

    /**
     * Invoked when an attempt to check out a connection from a pool fails. The default implementation does nothing.
     */
    public void connectionCheckOutFailed(ConnectionCheckOutFailedEvent event) {
        //logger.info("Check out failed: {}", event.getServerId());
    }

    /**
     * Invoked when a connection is checked in to a pool. The default implementation does nothing.
     */
    public void connectionCheckedIn(ConnectionCheckedInEvent event) {
        //logger.info("Check in: {}", event.getConnectionId());
    }


    /**
     * Invoked when a connection is created. The default implementation does nothing.
     */
    public void connectionCreated(ConnectionCreatedEvent event) {
        logger.info("Connection created: {}", event.getConnectionId());
    }

    /**
     * Invoked when a connection is ready for use. The default implementation does nothing.
     */
    public void connectionReady(ConnectionReadyEvent event) {
        logger.info("Connection ready: {}", event.getConnectionId());
    }


    /**
     * Invoked when a connection is removed from a pool. The default implementation does nothing.
     */
    public void connectionClosed(ConnectionClosedEvent event) {
        logger.info("Connection closed: {}", event.getConnectionId());
    }
}
