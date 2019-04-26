package uk.ac.susx.tag.norconex.controller;

// java imports
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

// logging imports
import com.norconex.importer.parser.GenericDocumentParserFactory;
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

	private static final String PROGRESS = "progress";
	private static final String LOGS = "logs";
	private final File crawlStore;

	private ContinuousCollectorFactory factory; 	// Factory that produces consistently configured crawlers for continuous running
	private ContinuousListener listener;		 	// Listens for the end of each crawl and restarts unless stop instruction given
	private ContinuousCollector collector; 		 	// Current collector which controls the crawling behaviour at run-time.
	private ContinuousCollectorListener collectorListener;
	private ContinuousEstimatorStore cacheStore;		// The store that contains the needed meta-data for each urls recrawl strategy
	private ContinuousCrawlerConfig config;			// the config that controls the crawl
	
	private BlockingQueue<HttpDocument> outputQueue;
	private String collectorId; 						// Used to store the id of the current collector;
	private String crawlerId;
	private boolean ignoreSiteMap;
	private final String storeLocation;

	private boolean finished;						// Has the crawler been requested to shutdown the crawl permanently


	// Used to create upper and lower bounds on crawl delays to prevent the statistics running out of control
	// (e.g. if for a instance a page never seems to change we still want to check it once in a while or changes so frequently the crawler polls the server too often)
	public enum Delay { DEFAULT, MINIMUM, MAXIMUM }
	
	public ContinuousController(String userAgent, File crawlStore, int depth, 
			List<String> urlRegex, int numCrawlers, boolean ignoreRobots,
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

		collectorId = "continuousCollector";
		crawlerId = "continuousCrawler";

		config = new ContinuousCrawlerConfig(userAgent,depth,numCrawlers,crawlStore,ignoreRobots,
				ignoreSiteMap,crawlerId,urlRegex,seed);

		config.setIgnoreSitemap(true);

		if (ignoreSiteMap) {
			// set our recrawlable resolver to check whether it is time to recrawl page
			ContinuousRecrawlableResolver recrawlableResolver = new ContinuousRecrawlableResolver(cacheStore);
			config.setRecrawlableResolver(recrawlableResolver);
		}

		
		// custom fetcher or postimporter to send to M52 queue
		config.setPostImportProcessors(new Method52PostProcessor(outputQueue),new ContinuousPostProcessor(cacheStore));
		collectorListener = new ContinuousCollectorListener(this);

		// Sets an empty doc parser so that the document remains in its raw content
		ImporterConfig ic = new ImporterConfig();
		ic.setMaxFileCacheSize(100);
		ic.setMaxFilePoolCacheSize(100);
		ic.setTempDir(crawlStore);
//		 For now - do not do any parsing on the discovered docs (i.e. all done by M52)
		GenericDocumentParserFactory gdpf = new GenericDocumentParserFactory();
		gdpf.setIgnoredContentTypesRegex(".*");
		ic.setParserFactory(gdpf);
		config.setImporterConfig(ic);
		
		// setup the collector config
		HttpCollectorConfig collectorConfig = ContinuousCollector.createCollectorConfig(collectorId, collectorListener);
		collectorConfig.setCrawlerConfigs(config);
		collectorConfig.setProgressDir(new File(crawlStore,PROGRESS).getAbsolutePath());
		collectorConfig.setLogsDir(new File(crawlStore,LOGS).getAbsolutePath());
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
		cacheStore.commit();
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
			
			config.setRecrawlableResolver(new ContinuousRecrawlableResolver(cacheStore));
			config.setPostImportProcessors(new ContinuousPostProcessor(cacheStore),new Method52PostProcessor(outputQueue));
			config.setId(crawlerId);
			HttpCollectorConfig collectorConfig = ContinuousCollector.createCollectorConfig(collectorId, collectorListener);
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
			logger.info("Restarting Collector.");
			controller.resetCollector();
			cacheStore.getGlobalMetadata().incrementCrawls();
			cacheStore.getGlobalMetadata().updateCrawlTime();
			logger.info("There have been a total of " + cacheStore.getGlobalMetadata().getTotalCrawls() + "crawls");
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

	public static void main(String[] args) {
		BlockingQueue<HttpDocument> queue = new ArrayBlockingQueue<HttpDocument>(10000);
//		String seed = "https://www.gamespot.com/forums/system-wars-314159282/as-the-entire-population-gains-basic-gaming-skills-33456501/";
		String seed = "http://www.taglaboratory.org";
//		String seed = "https://www.neogaf.com/threads/days-gone-ot-days-gone-b-gud.1478110/";
//		HttpCollectorConfig hcc = new HttpCollectorConfig();
//		hcc.setId(UUID.randomUUID().toString());
//		HttpCrawlerConfig config = BasicCollector.crawlerConfig("m52",-1,5,
//				new File("/Users/jp242/Documents/Projects/Crawler-Upgrade/testdb"),true,
//				true,UUID.randomUUID().toString(),new ArrayList<>(), new ArrayBlockingQueue<>(10000));
//		config.setStartURLs(seed);
//		hcc.setProgressDir("/Users/jp242/Documents/Projects/Crawler-Upgrade/testdb/progress");
//		hcc.setLogsDir("/Users/jp242/Documents/Projects/Crawler-Upgrade/testdb/logs");
//		hcc.setCrawlerConfigs(config);

		ContinuousController cc = new ContinuousController("M52",new File("/Users/jp242/Documents/Projects/Crawler-Upgrade/testdb"),-1,new ArrayList<>(),5,
				true,true, seed ,new ArrayBlockingQueue<>(1000));

		cc.start();
	}
}
