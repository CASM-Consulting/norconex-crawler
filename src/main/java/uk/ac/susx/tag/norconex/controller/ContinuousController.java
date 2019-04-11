package uk.ac.susx.tag.norconex.controller;

// java imports
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

// logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// norconex imports
import com.norconex.collector.core.ICollector;
import com.norconex.collector.core.ICollectorLifeCycleListener;
import com.norconex.collector.http.HttpCollectorConfig;
import com.norconex.collector.http.doc.HttpDocument;

import uk.ac.susx.tag.norconex.collector.ContinuousCollector;
import uk.ac.susx.tag.norconex.crawler.ContinuousCrawlerConfig;
import uk.ac.susx.tag.norconex.crawler.ContinuousRecrawlableResolver;
import uk.ac.susx.tag.norconex.crawler.ContinuousEstimatorStore;
import uk.ac.susx.tag.norconex.document.ContinuousPostProcessor;
import uk.ac.susx.tag.norconex.document.Method52PreProcessor;

/**
 * The controlling class for the entire continuous crawl process
 * @author jp242
 */
public class ContinuousController {
	
	protected static final Logger logger = LoggerFactory.getLogger(ContinuousController.class);
	
	public static final int BURNIN_CRAWLS = 20; 					// Number of crawls to perform before calculating custom page delays
	public static final int DEFAULT_THREADS = 5;					// Number of threads to use in the crawl
	
	public static final long DEFAULT_MIN_RECRAWL_DELAY  = 6;  		// The default recrawl delays in hours.
	public static final long DEFAULT_MAX_RECRAWL_DELAY  = 730;
	public static final long DEFAULT_DELAY 				= 12;

	private static final String PROGRESS = "progress";
	private final File crawlStore;

	private ContinuousCollectorFactory factory; 	// Factory that produces consistently configured crawlers for continuous running
	private ContinuousListener listener;		 	// Listens for the end of each crawl and restarts unless stop instruction given
	private ContinuousCollector collector; 		 	// Current collector which controls the crawling behaviour at run-time.
	private ContinuousCollectorListener collectorListener;
	private ContinuousEstimatorStore cacheStore;		// The store that contains the needed meta-data for each urls recrawl strategy
	private ContinuousCrawlerConfig config;			// the config that controls the crawl
	
	private BlockingQueue<HttpDocument> outputQueue;
	private String currentCollectorId; 						// Used to store the id of the current collector;
	private String currentCrawlerId;
	private boolean ignoreSiteMap;
	private final String storeLocation;

	private boolean finished;						// Has the crawler been requested to shutdown the crawl permanently


	// Used to create upper and lower bounds on crawl delays to prevent the statistics running out of control
	// (e.g. if for a instance a page never seems to change we still want to check it once in a while or changes so frequently the crawler polls the server too often)
	public enum Delay { DEFAULT, MINIMUM, MAXIMUM }
	
	public ContinuousController(String userAgent, File crawlStore, int depth, 
			List<String> urlRegex, int numCrawlers, boolean respectRobots, 
			boolean ignoreSiteMap, String seed, BlockingQueue<HttpDocument> queue) {
		
		listener = new ContinuousListener(this);
		storeLocation = new File(crawlStore,"conCache").getAbsolutePath();
		cacheStore = new ContinuousEstimatorStore(storeLocation);
		this.ignoreSiteMap = ignoreSiteMap;
		this.crawlStore = crawlStore;
		listener = new ContinuousListener(this);
		outputQueue = queue;
		finished = false;
		factory = new ContinuousCollectorFactory();
		
		currentCollectorId = UUID.randomUUID().toString();
		currentCrawlerId = UUID.randomUUID().toString();

		config = new ContinuousCrawlerConfig(userAgent,depth,numCrawlers,crawlStore,respectRobots,
				ignoreSiteMap,currentCrawlerId,urlRegex,seed);
		
		// set our recrawlable resolver to check whether it is time to recrawl page
		ContinuousRecrawlableResolver recrawlableResolver = new ContinuousRecrawlableResolver(ignoreSiteMap,cacheStore);
		config.setRecrawlableResolver(recrawlableResolver);
		
		// custom fetcher or postimporter to send to M52 queue
		config.setPreImportProcessors(new Method52PreProcessor(outputQueue));
		config.setPostImportProcessors(new ContinuousPostProcessor(cacheStore));
		collectorListener = new ContinuousCollectorListener(this);
		
		// setup the collector config
		HttpCollectorConfig collectorConfig = ContinuousCollector.createCollectorConfig(currentCollectorId, collectorListener);
		collectorConfig.setCrawlerConfigs(config);
		collectorConfig.setProgressDir(new File(crawlStore,PROGRESS).getAbsolutePath());
		collector = new ContinuousCollector(collectorConfig);
		
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
	public void start() {
		logger.info("INFO: Starting continuous crawl");
		collector.start(true);
	}
	
	/**
	 * Permanently shuts down the entire process.
	 * Does not wait for the current crawl to finish.
	 */
	public void shutdown() {
		logger.info("Shutting down continuous crawl");
		finished = true;
		collector.shutdown();
		cacheStore.close();
	}

	/**
	 * Resets the collector and starts the crawl again.
	 */
	private void resetCollector() {
		logger.info("Restarting crawler");
		collector = null;
		cacheStore.close();		// closed so previous crawl stats are committed to the store
		cacheStore = null;
		cacheStore = new ContinuousEstimatorStore(storeLocation);
		collector = factory.createCollector();

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
			
			currentCollectorId = UUID.randomUUID().toString();
			currentCrawlerId = UUID.randomUUID().toString();
			
			config.setRecrawlableResolver(new ContinuousRecrawlableResolver(ignoreSiteMap,cacheStore));
			config.setPreImportProcessors(new Method52PreProcessor(outputQueue));
			config.setPostImportProcessors(new ContinuousPostProcessor(cacheStore));
			config.setId(currentCrawlerId);
			HttpCollectorConfig collectorConfig = ContinuousCollector.createCollectorConfig(currentCollectorId, collectorListener);
			collectorConfig.setCrawlerConfigs(config);
			collectorConfig.setProgressDir(new File(crawlStore,PROGRESS).getAbsolutePath());
			collector = new ContinuousCollector(collectorConfig);

			return collector;
		}
	}

	/**
	 * Listener used to instruct the controller to restart the controller.
	 * @author jp242
	 */
	private class ContinuousListener {

		ContinuousController controller;

		public ContinuousListener(ContinuousController controller) {
			this.controller = controller;
		}

		public void restartCollector() {
			controller.resetCollector();
			cacheStore.getGlobalMetadata().incrementCrawls();
			cacheStore.getGlobalMetadata().updateCrawlTime();
			controller.start();
		}

	}
	
	/**
	 * Creates an infinite loop causing the crawler to continually start crawling again when finished.
	 * This can only be interrupted by manually requesting the crawler to stop permanently.
	 * @author jp242
	 */
	public class ContinuousCollectorListener implements ICollectorLifeCycleListener {
		
		private ContinuousController controller;
		
		public ContinuousCollectorListener(ContinuousController controller) {
			this.controller = controller;
		}

		public void onCollectorStart(ICollector collector) {}

		public void onCollectorFinish(ICollector collector) {
			if(!finished) {
				controller.getListener().restartCollector();		
			}
		}
		
	}

}
