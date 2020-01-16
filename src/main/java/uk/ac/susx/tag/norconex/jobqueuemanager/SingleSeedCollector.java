package uk.ac.susx.tag.norconex.jobqueuemanager;

// java imports
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

// logging imports
import com.beust.jcommander.JCommander;
import com.norconex.collector.http.HttpCollector;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.collector.http.recrawl.impl.GenericRecrawlableResolver;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// norconex imports
import com.norconex.collector.core.ICollector;
import com.norconex.collector.core.ICollectorLifeCycleListener;
import com.norconex.collector.http.HttpCollectorConfig;

import uk.ac.susx.tag.norconex.collector.ContinuousCollector;
import uk.ac.susx.tag.norconex.controller.ContinuousController;
import uk.ac.susx.tag.norconex.crawler.ContinuousCrawlerConfig;
import uk.ac.susx.tag.norconex.crawlstore.CompactCrawlDatabases;

/**
 * The controlling class for the entire continuous crawl process
 * @author jp242
 */
public class SingleSeedCollector {

    protected static final Logger logger = LoggerFactory.getLogger(ContinuousController.class);

    public static final String USERAGENT = "casm.jqm.polling.agent";
    public static final String FILTER = "casm.jqm.polling.filter";
    public static final String CRAWLB = "casm.jqm.polling.crawldb";
    public static final String DEPTH = "casm.jqm.polling.depth";
    public static final String POLITENESS = "casm.jqm.polling.politeness";
    public static final String SITEMAP = "casm.jqm.polling.sitemap";
    public static final String ROBOTS = "casm.jqm.polling.robots";
    public static final String ID = "casm.jqm.polling.id";
    public static final String THREADS = "casm.jqm.polling.threads";
    public static final String SEED = "casm.jqm.polling.seed";
    public static final String INDEXONLY = "casm.jqm.crawling.index";

    // crawl-store suffixes
    private static final String PROGRESS = "progress";
    private static final String LOGS = "logs";

    // parent directory for all crawl-store info.
    private  File crawlStore;
    private  CollectorConfigurationFactory configFactory;
    private  String userAgent;
    private  int threadsPerSeed;
    private  boolean ignoreRobots;
    private  int depth;
    private  String urlRegex;
    private  String seed;
    private  long politeness;
    private  HttpCrawlerConfig config;

    // Collector components
    private SingleSeedCollectorFactory factory; 	// Factory that produces consistently configured crawlers for continuous running

    // collector id - remains static so that the cache is not lost between runs.
    private  String collectorId;
    private static final String CRAWLER_ID = "singleSeedCollector";

    // standard params
    private boolean ignoreSiteMap;

    // Has the crawler been requested to shutdown the crawl permanently?
    private boolean finished;

    public SingleSeedCollector(String userAgent, File crawlStore, String id,
                               int depth, String urlRegex, int threadsPerSeed, boolean ignoreRobots,
                               boolean ignoreSiteMap, long politenesDelay, String seed) {

        this.ignoreSiteMap = ignoreSiteMap;
        URL url = null;
        try {
            url = new URL(seed);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed due to malformed URL: " + seed);
        }

        String domain =  (url.getHost().startsWith("www")) ? url.getHost().substring(4) : url.getHost();
        this.crawlStore = new File(crawlStore,domain);
        finished = false;
        collectorId = id;

        this.userAgent = userAgent;
        this.threadsPerSeed = threadsPerSeed;
        this.ignoreRobots = ignoreRobots;
        this.depth = depth;
        this.urlRegex = urlRegex;
        this.seed = seed;
        this.politeness = politenesDelay;

        factory = new SingleSeedCollectorFactory();
        configFactory = new CollectorConfigurationFactory();

        try {
            config = configFactory.createConfiguration();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed due to malformed URL: " + seed);
        }

    }

    // returns the crawler configuration file
    public HttpCrawlerConfig getConfiguration() {
        return config;
    }

    public void setConfiguration(HttpCrawlerConfig config) {
        this.config = config;
    }

    /**
     * Creates and starts the crawler
     * This crawler can be restarted if it fails.
     */
    public void start() throws RuntimeException, URISyntaxException {
        logger.info("Running crawl for seed: " + seed);
        HttpCollector collector = factory.createCollector();
        collector.start(true);
    }

    /**
     * Permanently shuts down the entire process.
     */
    public void shutdown() throws InterruptedException {
        logger.info("Shutting down crawler");
        finished = true;
    }

    /**
     * Creates collectors for each continuous run of the crawler;
     * @author jp242
     */
    public class SingleSeedCollectorFactory {

        public ContinuousCollector createCollector() throws URISyntaxException {

            HttpCollectorConfig collectorConfig = new HttpCollectorConfig();
            collectorConfig.setId(collectorId);

            collectorConfig.setCrawlerConfigs(config);
            collectorConfig.setCollectorListeners(new CleanUpCollector());

            collectorConfig.setProgressDir(new File(crawlStore,PROGRESS).getAbsolutePath());
            collectorConfig.setLogsDir(new File(crawlStore,LOGS).getAbsolutePath());
            return new ContinuousCollector(collectorConfig);

        }

    }

    public class CollectorConfigurationFactory {

        public HttpCrawlerConfig createConfiguration() throws URISyntaxException {

            // First - level validate the URL
            if(!UrlValidator.getInstance().isValid(seed)){
                throw new URISyntaxException(seed,"Invalid URL composition");
            }

            // Second - get the domain to use as crawler id
            URI url = new URI(seed);
            String host = url.getHost();
            String domain = (host.startsWith("www")) ? host.substring(4) : host;


            ContinuousCrawlerConfig config = new ContinuousCrawlerConfig(userAgent, depth, threadsPerSeed, crawlStore, ignoreRobots,
                    ignoreSiteMap, domain + "_" + CRAWLER_ID, urlRegex, politeness, seed);

            return config;
        }

    }

    public class CleanUpCollector implements ICollectorLifeCycleListener {

        @Override
        public void onCollectorStart(ICollector collector) {
            // not needed.
        }

        @Override
        public void onCollectorFinish(ICollector collector) {
            logger.error("Deleting logs and compacting databases.");
            String logLocation = new File(crawlStore,LOGS).getAbsolutePath();
            try {
                deleteLogs(Paths.get(logLocation));
            } catch (IOException e) {
                e.printStackTrace();
            }
            CompactCrawlDatabases ccd = new CompactCrawlDatabases();
            ccd.walkAndCompactDatabases(crawlStore.toPath());
            logger.error("Log deletiong and crawldb compact complete.");
        }
    }

    public static void deleteLogs(Path logDir) throws IOException {
        try(Stream<Path> walk = Files.walk(logDir)) {
            walk.filter(path -> path.endsWith(".log"))
                    .forEach(path1 -> {
                        try {
                            Files.delete(path1);
                        } catch (IOException e) {
                            logger.error(e.getMessage());
                        }
                    });
        }
    }

	public static void main(String[] args) {

        for(String arg : args) {
            logger.error(arg + " ");
        }

        List<String> splitArgs = new ArrayList<>();
        for(String arg : args){
            splitArgs.addAll(Arrays.asList(arg.split("\\s+")));
        }
        String[] corrArgs = splitArgs.toArray(new String[splitArgs.size()]);
        CrawlerArguments ca = new CrawlerArguments();
        new JCommander().newBuilder()
                .addObject(ca)
                .build()
                .parse(corrArgs);

        SingleSeedCollector cc = new SingleSeedCollector(ca.userAgent,new File(ca.crawldb), ca.id,
                ca.depth, ca.urlFilter,ca.threadsPerSeed,ca.ignoreRobots,
                ca.ignoreSitemap, ca.polite,
                ca.seeds.get(0));

        try {
            cc.start();
        } catch (URISyntaxException e) {
            logger.error("Error attempting to start crawler");
            throw new RuntimeException("The provided URL was invalid");
        }

    }

}
