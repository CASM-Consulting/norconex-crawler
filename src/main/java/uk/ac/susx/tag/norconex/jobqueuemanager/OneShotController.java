package uk.ac.susx.tag.norconex.jobqueuemanager;

// java imports
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

// logging imports
import com.beust.jcommander.JCommander;
import com.enioka.jqm.api.JobManager;
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
import uk.ac.susx.tag.norconex.crawler.ContinuousRecrawlableResolver;
import uk.ac.susx.tag.norconex.crawlstore.ContinuousEstimatorStore;
import uk.ac.susx.tag.norconex.document.ContinuousPostProcessor;

/**
 * The controlling class for the entire continuous crawl process
 * @author jp242
 */
public class OneShotController {

    private JobManager manager;

    protected static final Logger logger = LoggerFactory.getLogger(ContinuousController.class);

    public static final int BURNIN_CRAWLS = 20; 					// Number of crawls to perform before calculating custom page delays

    public static final long DEFAULT_MIN_RECRAWL_DELAY  = 6;  		// The default recrawl delays in hours.
    public static final long DEFAULT_MAX_RECRAWL_DELAY  = 730;
    public static final long DEFAULT_DELAY 				= 12;

    public static final long SCHEDULE_DELAY_SECONDS = 10;			// The delay between each scehduled recrawl

    // crawl-store suffixes
    private static final String PROGRESS = "progress";
    private static final String LOGS = "logs";

    // parent directory for all crawl-store info.
    private final File crawlStore;
    private final CollectorConfigurationFactory configFactory;
    private final String userAgent;
    private final int threadsPerSeed;
    private final boolean ignoreRobots;
    private final int depth;
    private final List<String> urlRegex;
    private final String seed;
    private final long politeness;

    // Collector components
    private final ContinuousCollectorFactory factory; 	// Factory that produces consistently configured crawlers for continuous running
//    private final ContinuousListener listener;		 	// Listens for the end of each crawl and restarts unless stop instruction given
    private final ContinuousEstimatorStore cacheStore;	// The store that contains the needed meta-data for each urls recrawl strategy

    // Queue to send output
//    private final BlockingQueue<String> outputQueue;

    // collector id - remains static so that the cache is not lost between runs.
    private final String collectorId;
    private static final String CRAWLER_ID = "continuousCrawler";

    // Scheduler for crawling delay
    final ScheduledExecutorService scheduler;

    // standard params
    private boolean ignoreSiteMap;
    private final String storeLocation;

    // Has the crawler been requested to shutdown the crawl permanently?
    private boolean finished;


    // Used to create upper and lower bounds on crawl delays to prevent the statistics running out of control
    // (e.g. if for a instance a page never seems to change we still want to check it once in a while or changes so frequently the crawler polls the server too often)
    public enum Delay { DEFAULT, MINIMUM, MAXIMUM }

    public enum Status { COMPLETE, FAILED }

    public OneShotController(String userAgent, File crawlStore, String id,
                                int depth, List<String> urlRegex, int threadsPerSeed, boolean ignoreRobots,
                                boolean ignoreSiteMap, long politenesDelay, String seed) {

        storeLocation = new File(crawlStore,"conCache").getAbsolutePath();
        cacheStore = new ContinuousEstimatorStore(storeLocation);
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

        factory = new ContinuousCollectorFactory();
        configFactory = new CollectorConfigurationFactory();

        scheduler = Executors.newScheduledThreadPool(1);

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
    public void start() throws RuntimeException {
        logger.info("Sheduling first crawl");
        try {
            scheduleNextCrawl(1);
            scheduler.awaitTermination(Long.MAX_VALUE,TimeUnit.HOURS);
        }
        catch (Exception e){
            throw new RuntimeException("Failed when attempting to shedule a crawl");
        }

    }

    private void scheduleNextCrawl(long delaySeconds) {

        scheduler.schedule(new SheduledCrawl(), delaySeconds, TimeUnit.SECONDS);

    }


    /**
     * Permanently shuts down the entire process.
     */
    public void shutdown() throws InterruptedException {
        logger.info("Shutting down continuous crawl");
        finished = true;
        scheduler.shutdown();
        cacheStore.close();
    }

    /**
     * Creates collectors for each continuous run of the crawler;
     * @author jp242
     */
    public class ContinuousCollectorFactory {

        public ContinuousCollector createCollector() {

            ContinuousCollectorListener ccl = new ContinuousCollectorListener();
            HttpCollectorConfig collectorConfig = new HttpCollectorConfig();
            collectorConfig.setId(collectorId);
            try {
                collectorConfig.setCrawlerConfigs(configFactory.createConfigurations());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Seed URL is invalid");
            }
            collectorConfig.setProgressDir(new File(crawlStore,PROGRESS).getAbsolutePath());
            collectorConfig.setLogsDir(new File(crawlStore,LOGS).getAbsolutePath());
            return new ContinuousCollector(collectorConfig);
        }

    }

    public class CollectorConfigurationFactory {

        public HttpCrawlerConfig[] createConfigurations() throws URISyntaxException {

            List<HttpCrawlerConfig> configs = new ArrayList<>();

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

            if (ignoreSiteMap) {
                ContinuousRecrawlableResolver crr = new ContinuousRecrawlableResolver(cacheStore);
                config.setRecrawlableResolver(crr);
            }

            // custom fetcher or postimporter to send to M52 queue
            config.setPostImportProcessors(new ContinuousPostProcessor(cacheStore));

            configs.add(config);

            return configs.toArray(new HttpCrawlerConfig[configs.size()]);

        }

    }



    /**
     * A simple runnable that
     */
    public class SheduledCrawl implements Runnable {

        @Override
        public void run() {
            // setup the collector config
            ContinuousCollector collector = factory.createCollector();
            collector.start(false);
        }

    }

    /**
     * Creates an infinite loop causing the crawler to continually start crawling again when finished.
     * This can only be interrupted by manually requesting the crawler to stop permanently.
     * @author jp242
     */
    public class ContinuousCollectorListener implements ICollectorLifeCycleListener {

        public void onCollectorStart(ICollector collector) {}

        public void onCollectorFinish(ICollector collector) {
            manager.sendMsg(Status.COMPLETE.toString());
        }
    }

    //Include JCommander interface for parsing the inputs!!!
	public static void main(String[] args) {

        CrawlerArguments ca = new CrawlerArguments();
        new JCommander().newBuilder()
                .addObject(ca)
                .build()
                .parse(args);

        OneShotController cc = new OneShotController(ca.userAgent,new File(ca.crawldb), ca.id,
                ca.depth, ca.urlFilters,ca.threadsPerSeed,ca.ignoreRobots,
                ca.ignoreSitemap, ca.polite,
                ca.seeds.get(0));

		cc.start();

	}

}
