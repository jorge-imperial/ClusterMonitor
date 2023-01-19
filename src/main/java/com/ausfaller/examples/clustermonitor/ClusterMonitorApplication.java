package com.ausfaller.examples.clustermonitor;



import com.mongodb.*;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.PemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;



// This disables default host monitoring (DefaultServerMonitor)
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class})
public class ClusterMonitorApplication implements CommandLineRunner  {

    private static final int THREAD_COUNT = 150;

    List<ServerAddress> atlasMongos = Arrays.asList(
            new ServerAddress("cluster44-shard-00-00.hqulf.mongodb.net", 27016),
            new ServerAddress("cluster44-shard-00-01.hqulf.mongodb.net", 27016),
            new ServerAddress("cluster44-shard-00-02.hqulf.mongodb.net", 27016),
            new ServerAddress("cluster44-shard-01-00.hqulf.mongodb.net", 27016),
            new ServerAddress("cluster44-shard-01-01.hqulf.mongodb.net", 27016),
            new ServerAddress("cluster44-shard-01-02.hqulf.mongodb.net", 27016)
    );

    List<ServerAddress> onPremHosts = Arrays.asList(
            new ServerAddress("m04.ausfaller.com", 27016),
            new ServerAddress("m05.ausfaller.com", 27016),
            new ServerAddress("m06.ausfaller.com", 27016),
            new ServerAddress("m07.ausfaller.com", 27016),
            new ServerAddress("m08.ausfaller.com", 27016),
            new ServerAddress("m09.ausfaller.com", 27016)
    );

    private static Logger logger = LogManager.getLogger(ClusterMonitorApplication.class);
    private static int maxConnections = 50;
    private static int maxWaitTime = 10000;

    public static void main(String[] args) {

        Properties props = System.getProperties();
        logger.info("Current working directory is " + props.getProperty("user.dir"));

        SpringApplication.run(ClusterMonitorApplication.class, args);
    }

    MongoClient mongoClient;
    @Override
    public void run(String... args) throws Exception {

        MongoCredential credential = MongoCredential.createScramSha1Credential("root", "admin", "P4ssw0rd".toCharArray());
        WorkerThread[] workers = new WorkerThread[THREAD_COUNT];

        Boolean on_prem = true;

        if (on_prem) {
            logger.info("Connecting to on-prem environment");

            // We need an SSL context for on-prem environment using self-signed certificates.
            X509ExtendedKeyManager keyManager = PemUtils.loadIdentityMaterial(Paths.get("/etc/ssl/M-C756C1J67D.pem"));
            X509ExtendedTrustManager trustManager = PemUtils.loadTrustMaterial(Paths.get("/etc/ssl/rootCA.pem"));
            SSLFactory sslFactory = SSLFactory.builder()
                    .withIdentityMaterial(keyManager)
                    .withTrustMaterial(trustManager)
                    .build();
            SSLContext sslContext = sslFactory.getSslContext();

            MongoClientOptions options = new MongoClientOptions.Builder()
                    .connectionsPerHost(maxConnections)
                    .connectTimeout(30000)
                    .socketTimeout(60000)
                    .maxWaitTime(maxWaitTime)
                    .sslEnabled(true)
                    .sslContext(sslContext)
                    .sslInvalidHostNameAllowed(true)
                    .writeConcern(WriteConcern.MAJORITY)
                    .retryWrites(true)
                    .readPreference(ReadPreference.primary())
                    .addServerListener(new MongoServerListener("ONPREM"))
                    .addConnectionPoolListener(new MongoConnectionListener())
                    .build();

            mongoClient = new MongoClient(onPremHosts, credential, options);
        } else {

            MongoClientOptions options = new MongoClientOptions.Builder()
                    .connectionsPerHost(maxConnections)
                    .connectTimeout(30000)
                    .socketTimeout(60000)
                    .maxWaitTime(maxWaitTime)
                    .sslEnabled(true)
                    .writeConcern(WriteConcern.MAJORITY)
                    .retryWrites(true)
                    .readPreference(ReadPreference.primary())
                    .addServerListener(new MongoServerListener("ATLAS"))
                    .addConnectionPoolListener(new MongoConnectionListener())
                    .build();


            mongoClient = new MongoClient(atlasMongos, credential, options);
        }

        for (int i = 0; i < THREAD_COUNT; ++i) {
            workers[i] = new WorkerThread(i, mongoClient);

            // ramp up connections
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            workers[i].start();
        }
    }
}