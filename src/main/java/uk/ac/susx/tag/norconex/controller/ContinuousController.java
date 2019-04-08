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
import uk.ac.susx.tag.norconex.crawler.ContinuousStatsStore;
import uk.ac.susx.tag.norconex.document.ContinuousPostProcessor;
import uk.ac.susx.tag.norconex.document.Method52PostProcessor;

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
	 
	private ContinuousCollectorFactory factory; 	// Factory that produces consistently configured crawlers for continuous running
	private ContinuousListener listener;		 	// Listens for the end of each crawl and restarts unless stop instruction given
	private ContinuousCollector collector; 		 	// Current collector which controls the crawling behaviour at run-time.
	private ContinuousCollectorListener collectorListener;
	private ContinuousStatsStore cacheStore;		// The store that contains the needed meta-data for each urls recrawl strategy
	private ContinuousCrawlerConfig config;			// the config that controls the crawl
	
	private BlockingQueue<HttpDocument> outputQueue;
	private String currentCollectorId; 						// Used to store the id of the current collector;
	private String currentCrawlerId;
	private boolean ignoreSiteMap;
	private final String storeLocation;

	private boolean finished;						// Has the crawler been requested to shutdown the crawl permanently

	
	public enum Delay {DEFAULT,MINIMUM, MAXIMUM} 
	
	public ContinuousController(String userAgent, File crawlStore, int depth, 
			List<String> urlRegex, int numCrawlers, boolean respectRobots, 
			boolean ignoreSiteMap, String seed, BlockingQueue<HttpDocument> queue) {
		
		listener = new ContinuousListener(this);
		this.storeLocation = new File(crawlStore,"conCache").getAbsolutePath();
		cacheStore = new ContinuousStatsStore(storeLocation);
		this.ignoreSiteMap = ignoreSiteMap;
		listener = new ContinuousListener(this);
		outputQueue = queue;
		finished = false;
		
		currentCollectorId = UUID.randomUUID().toString();
		currentCrawlerId = UUID.randomUUID().toString();

		config = new ContinuousCrawlerConfig(userAgent,depth,numCrawlers,crawlStore,respectRobots,
				ignoreSiteMap,currentCrawlerId,urlRegex,seed);
		
		// set our recrawlable resolver MAYBE needed instead of delay - look into 
		ContinuousRecrawlableResolver recrawlableResolver = new ContinuousRecrawlableResolver(ignoreSiteMap,cacheStore);
		config.setRecrawlableResolver(recrawlableResolver);
		
		// custom fetcher or postimporter to send to M52 queue
		config.setPostImportProcessors(new ContinuousPostProcessor(cacheStore), new Method52PostProcessor(outputQueue));
		collectorListener = new ContinuousCollectorListener(this);
		
		// setup the collector config
		HttpCollectorConfig collectorConfig = ContinuousCollector.createCollectorConfig(currentCollectorId, collectorListener);
		collectorConfig.setCrawlerConfigs(config);
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
		collector.start(false);
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
		collector.stop();
		cacheStore.close();
		cacheStore = null; 
		collector = null;
		cacheStore = new ContinuousStatsStore(storeLocation);
		collector = factory.createCollector();

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
			controller.start();
		}
		
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
			config.setPostImportProcessors(new ContinuousPostProcessor(cacheStore), new Method52PostProcessor(outputQueue));
			config.setId(currentCrawlerId);
			HttpCollectorConfig collectorConfig = ContinuousCollector.createCollectorConfig(currentCollectorId, collectorListener);
			collectorConfig.setCrawlerConfigs(config);
			collector = new ContinuousCollector(collectorConfig);

			return collector;
		}
	}
	
	/**
	 * Creates an infinite loop causing the crawler to continually start crawling again when finished.
	 * This can only be interrupted by manually requesting the crawler to stop permanently.
	 * @author jp242
	 *
	 */
	public class ContinuousCollectorListener implements ICollectorLifeCycleListener {
		
		private ContinuousController controller;
		
		public ContinuousCollectorListener(ContinuousController controller) {
			this.controller = controller;
		}

		@Override
		public void onCollectorStart(ICollector collector) {}

		@Override
		public void onCollectorFinish(ICollector collector) {
			if(!finished) {
				controller.getListener().restartCollector();		
			}
		}
		
	}

}
