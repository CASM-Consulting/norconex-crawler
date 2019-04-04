package uk.ac.susx.tag.norconex.controller;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.ICollector;
import com.norconex.collector.core.ICollectorLifeCycleListener;

import uk.ac.susx.tag.norconex.collector.ContinuousCollector;
import uk.ac.susx.tag.norconex.crawler.ContinuousCrawlerConfig;

/**
 * The controlling class for the entire continuous crawl process
 * @author jp242
 */
public class ContinuousController {
	
	protected static final Logger logger = LoggerFactory.getLogger(ContinuousController.class);
	
	public static final int BURNIN_CRAWLS = 30; 					// Number of crawls to perform before calculating custom page delays
	public static final long DEFAULT_RECRAWL_CONSTANT = 43200000;  	// The default delay (12hrs) between re-crawling a specific web page.
	public static final int DEFAULT_THREADS = 5;					// Default number of threads to use if none given
	
	// metadata keys
	public static final String CRAWL_COUNT = "crawlCount";	// Number of times a web page has been recrawled
	public static final String FRESHNESS = 	"freshness"; 	// freshness score used to estimate required recrawl frequency
 
	private ContinuousCollectorFactory factory; 	// Factory that produces consistently configured crawlers for continuous running
	private ContinuousListener listener;		 	// Listens for the end of each crawl and restarts unless stop instruction given
	private ContinuousCollector collector; 		 	// Current collector which controls the crawling behaviour at run-time.
	
	private String currentId; 						// Used to store the id of the current collector;
	private boolean debugMode;						// Used to control verbose logging of crawl delays to assess functionality
	
	public ContinuousController(String userAgent, File crawlStore, int depth, 
			List<String> urlRegex, int numCrawlers, boolean respectRobots, double rate) {
		listener = new ContinuousListener(this);
		debugMode = false;
	}
	
	public void crawl() {
		logger.info("INFO: Starting continuous crawl");
	}
	
	/**
	 * Setting whether to verbosely log the delay queue for functionality assessment
	 * @param debug
	 */
	public void setDebugMode(boolean debug) {
		debugMode = debug;
	}
	
	/**
	 * Permanently shuts down the entire process.
	 * Does not wait for the current crawl to finish.
	 */
	public void shutdown() {
		logger.info("Shutting down continuous crawl");
		collector.shutdown();
	}

	/**
	 * Resets the collector and starts the crawl again.
	 */
	private void resetCollector() {
		collector.stop();
		collector = null;
		collector = factory.createCollector();
	}

	/**
	 * Listener used to instruct the controller to restart the controller.
	 * @author jp242
	 */
	public class ContinuousListener {
		
		ContinuousController controller;
		
		public ContinuousListener(ContinuousController controller) {
			this.controller = controller;
		}
		
		public void restartCollector() {
			controller.resetCollector();
			controller.crawl();
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
			
			final String collectorId = UUID.randomUUID().toString();
			final String crawlerId = UUID.randomUUID().toString();
			
//			ContinuousCrawlerConfig ccc = new ContinuousCrawlerConfig(user);
			
			return null;
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
			collector.stop();
			controller.getListener().restartCollector();		
		}
		
	}

}
