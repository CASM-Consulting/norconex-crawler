package uk.ac.susx.tag.norconex.jobqueuemanager;

// java imports
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

// logging imports
import com.beust.jcommander.JCommander;
import com.norconex.collector.http.HttpCollector;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
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

//    public static final int BURNIN_CRAWLS = 20; 					// Number of crawls to perform before calculating custom page delays

    public static final long DEFAULT_MIN_RECRAWL_DELAY  = 6;  		// The default recrawl delays in hours.
    public static final long DEFAULT_MAX_RECRAWL_DELAY  = 730;
    public static final long DEFAULT_DELAY 				= 12;

//    public static final long SCHEDULE_DELAY_SECONDS = 10;			// The delay between each scehduled recrawl

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
//    private ContinuousEstimatorStore cacheStore;	// The store that contains the needed meta-data for each urls recrawl strategy

    // collector id - remains static so that the cache is not lost between runs.
    private  String collectorId;
    private static final String CRAWLER_ID = "singleSeedCollector";

    // standard params
    private boolean ignoreSiteMap;
    private String storeLocation;

    // Has the crawler been requested to shutdown the crawl permanently?
    private boolean finished;


//    // Used to create upper and lower bounds on crawl delays to prevent the statistics running out of control
//    // (e.g. if for a instance a page never seems to change we still want to check it once in a while or changes so frequently the crawler polls the server too often)
    public enum Delay { DEFAULT, MINIMUM, MAXIMUM }

    public enum Status { START, COMPLETE, FAILED }

    public SingleSeedCollector(String userAgent, File crawlStore, String id,
                               int depth, String urlRegex, int threadsPerSeed, boolean ignoreRobots,
                               boolean ignoreSiteMap, long politenesDelay, String seed) {

        storeLocation = new File(crawlStore,"conCache").getAbsolutePath();
//        cacheStore = new ContinuousEstimatorStore(storeLocation);
        this.ignoreSiteMap = ignoreSiteMap;
        this.crawlStore = crawlStore;
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
            //TODO:error handling
        }

    }

    public HttpCrawlerConfig getConfiguration() {
        return config;
    }

    public void setConfiguration(HttpCrawlerConfig config) {
        this.config = config;
    }

    public static long getDelay(Delay delay) {
        long d = 0;
        switch(delay) {
            case DEFAULT:
                d = TimeUnit.HOURS.toMillis(DEFAULT_DELAY);
                break;
            case MINIMUM:
                d = TimeUnit.HOURS.toMillis(DEFAULT_MAX_RECRAWL_DELAY);
                break;
            case MAXIMUM:
                d = TimeUnit.HOURS.toMillis(DEFAULT_MIN_RECRAWL_DELAY);
                break;
        }
        return d;
    }

    /**
     * Start a continuous crawl
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
//        cacheStore.close();
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

            collectorConfig.setProgressDir(new File(crawlStore,PROGRESS).getAbsolutePath());
            collectorConfig.setLogsDir(new File(crawlStore,LOGS).getAbsolutePath());
            return new ContinuousCollector(collectorConfig);

        }

    }

    public class CollectorConfigurationFactory {

        public HttpCrawlerConfig createConfiguration() throws URISyntaxException {

//            List<HttpCrawlerConfig> configs = new ArrayList<>();

            // First - level validate the URL
            if(!UrlValidator.getInstance().isValid(seed)){
                throw new URISyntaxException(seed,"Invalid URL composition");
            }

            // Second - get the domain to use as crawler id
            URI url = new URI(seed);
            String host = url.getHost();
            String domain = (host.startsWith("www")) ? host.substring(4,host.length()) : host;


            ContinuousCrawlerConfig config = new ContinuousCrawlerConfig(userAgent, depth, threadsPerSeed, crawlStore, ignoreRobots,
                    ignoreSiteMap, domain + "_" + CRAWLER_ID, urlRegex, politeness, seed);

//            if (ignoreSiteMap) {
//                ContinuousRecrawlableResolver crr = new ContinuousRecrawlableResolver(cacheStore);
//                config.setRecrawlableResolver(crr);
//            }

            // custom fetcher or postimporter to send to M52 queue
//            config.setPostImportProcessors(new ContinuousPostProcessor(cacheStore));

//            configs.add(config);

            return config;

        }

    }

    /**
     * Sends a message via the JobManager to communicate upstream
     * @author jp242
     */
    public class SingleSeedCollectorListener implements ICollectorLifeCycleListener {

        public void onCollectorStart(ICollector collector) {
//            jm.sendMsg(seed + "_" + Status.START.toString());
        }

        public void onCollectorFinish(ICollector collector) {
//            jm.sendMsg(seed + "_" + Status.COMPLETE.toString());
//            cacheStore.getGlobalMetadata().incrementCrawls();
//            cacheStore.getGlobalMetadata().updateCrawlTime();
//            cacheStore.commit();
//            cacheStore.close();
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
