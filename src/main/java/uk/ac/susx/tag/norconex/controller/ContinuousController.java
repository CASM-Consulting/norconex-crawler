package uk.ac.susx.tag.norconex.controller;

// java imports
import java.io.*;
import java.lang.management.MemoryUsage;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// logging imports
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.commons.lang.MemoryUtil;
import com.norconex.importer.parser.GenericDocumentParserFactory;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// norconex imports
import com.norconex.collector.core.ICollector;
import com.norconex.collector.core.ICollectorLifeCycleListener;
import com.norconex.collector.http.HttpCollectorConfig;
import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.importer.ImporterConfig;

import uk.ac.susx.tag.norconex.collector.ContinuousCollector;
import uk.ac.susx.tag.norconex.crawler.ContinuousCrawlerConfig;
import uk.ac.susx.tag.norconex.crawler.ContinuousRecrawlableResolver;
import uk.ac.susx.tag.norconex.crawlstore.ContinuousEstimatorStore;
import uk.ac.susx.tag.norconex.document.ContinuousPostProcessor;
import uk.ac.susx.tag.norconex.document.Method52PostProcessor;

/**
 * The controlling class for the entire continuous crawl process
 * @author jp242
 */
public class ContinuousController {
	
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
	private final String[] seeds;
	private final long politeness;

	// Collector components
	private final ContinuousCollectorFactory factory; 	// Factory that produces consistently configured crawlers for continuous running
	private final ContinuousListener listener;		 	// Listens for the end of each crawl and restarts unless stop instruction given
	private final ContinuousEstimatorStore cacheStore;	// The store that contains the needed meta-data for each urls recrawl strategy

	// Queue to send output
	private final BlockingQueue<String> outputQueue;

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
	
	public ContinuousController(String userAgent, File crawlStore, String id,
								int depth, List<String> urlRegex, int threadsPerSeed, boolean ignoreRobots,
								boolean ignoreSiteMap, BlockingQueue<String> queue, long politenesDelay, String... seeds) {
		
		listener = new ContinuousListener();
		storeLocation = new File(crawlStore,"conCache").getAbsolutePath();
		cacheStore = new ContinuousEstimatorStore(storeLocation);
		this.ignoreSiteMap = ignoreSiteMap;
		this.crawlStore = crawlStore;
		outputQueue = queue;
		finished = false;
		collectorId = id;

		this.userAgent = userAgent;
		this.threadsPerSeed = threadsPerSeed;
		this.ignoreRobots = ignoreRobots;
		this.depth = depth;
		this.urlRegex = urlRegex;
		this.seeds = seeds;
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
	
	public ContinuousListener getListener() {
		return listener;
	}
	
	/**
	 * Creates collectors for each continuous run of the crawler;
	 * @author jp242
	 */
	public class ContinuousCollectorFactory {
		
		public ContinuousCollector createCollector() {

			ContinuousCollectorListener ccl = new ContinuousCollectorListener();
			HttpCollectorConfig collectorConfig = ContinuousCollector.createCollectorConfig(collectorId, ccl);
			collectorConfig.setCrawlerConfigs(configFactory.createConfigurations());
			collectorConfig.setProgressDir(new File(crawlStore,PROGRESS).getAbsolutePath());
			collectorConfig.setLogsDir(new File(crawlStore,LOGS).getAbsolutePath());
			return new ContinuousCollector(collectorConfig);
		}
	}



	public class CollectorConfigurationFactory {

		public HttpCrawlerConfig[] createConfigurations() {

			List<HttpCrawlerConfig> configs = new ArrayList<>();

			int id = 0;

			for(String seed : seeds) {

				// First - level validate the URL
				if(!UrlValidator.getInstance().isValid(seed)){
					logger.warn("WARN: Skipped invalid seed URL: " + seed);
					continue;
				}

				// Second - get the domain to use as crawler id
				String domain;
				try {
					URI url = new URI(seed);
					String host = url.getHost();
					domain = (host.startsWith("www")) ? host.substring(4,host.length()) : host;
				} catch (URISyntaxException e) {
					logger.warn("WARN: Skipped invalid seed URL: " + seed);
					continue;
				}


				ContinuousCrawlerConfig config = new ContinuousCrawlerConfig(userAgent, depth, threadsPerSeed, crawlStore, ignoreRobots,
						ignoreSiteMap, domain + "_" + CRAWLER_ID + "_"+ id, urlRegex, politeness, seed);

				if (ignoreSiteMap) {
					ContinuousRecrawlableResolver crr = new ContinuousRecrawlableResolver(cacheStore);
					config.setRecrawlableResolver(crr);
				}

				// custom fetcher or postimporter to send to M52 queue
				config.setPostImportProcessors(new Method52PostProcessor(outputQueue), new ContinuousPostProcessor(cacheStore));

				configs.add(config);

				id++;
			}

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
	 * Listener used to instruct the controller to restart the controller.
	 * @author jp242
	 */
	private class ContinuousListener {

		public void restart() {
			logger.info("Restarting Collector.");
			cacheStore.getGlobalMetadata().incrementCrawls();
			cacheStore.getGlobalMetadata().updateCrawlTime();
			cacheStore.commit();
			scheduleNextCrawl(SCHEDULE_DELAY_SECONDS);
			logger.info("There have been a total of " + cacheStore.getGlobalMetadata().getTotalCrawls() + " crawls");
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
			if(!finished) {
				getListener().restart();
			}
		}
	}

//	public static void main(String[] args) {
//
//		ContinuousController cc = new ContinuousController("m52",new File("/Users/jp242/Documents/Projects/Crawler-Upgrade/testdb"), "test",2,Arrays.asList(".*get-support/message-boards.*"),1,true,true,new ArrayBlockingQueue<>(10000),300,
//				"https://www.childline.org.uk/get-support/message-boards/boards/threads/thread/?messageid=164955");
//		cc.start();
//
//	}

	public static void main(String[] args) {

		List<String> seeds = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(args[0]))){
			br.lines()
					.filter(seed -> UrlValidator.getInstance().isValid(seed))
					.collect(Collectors.toList());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ContinuousController cc = new ContinuousController("m52",new File("/Users/jp242/Documents/Projects/Crawler-Upgrade/testdb"), "test",2,Arrays.asList(".*get-support/message-boards.*"),1,true,true,new ArrayBlockingQueue<>(10000),300,
				seeds.toArray(new String[seeds.size()]));
		cc.start();

	}


}
