package uk.ac.susx.tag.norconex.collector;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import org.apache.commons.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.http.HttpCollector;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;

public class BasicCollector extends HttpCollector {
	
	protected static final Logger logger = LoggerFactory.getLogger(BasicCollector.class);
	
//	private HttpCollectorConfig config;	
//	private HttpCollector collector;	// Core component overseeing the crawl
	
//	private String userAgent;			// Tell people who is crawling
	
//	public BasicCollector(HttpCollectorConfig config) {
		
////		this.userAgent = userAgent;
////		super.new
////		config = new HttpCollectorConfig();
////		config.setId(userAgent);
//	}
	
//	/**
//	 * Elegant shutdown of crawler
//	 */
//	public void stopCrawl() {
//		if(collector != null) {
//			collector.stop();
//		}
//	}
	
	// might need to implement
	
	
	/**
	 * Start a new crawl
	 * @param queue
	 * @param crawlerConfig
	 * @param resume
	 * @param seeds
	 * @return
	 */
	public boolean crawl(BlockingQueue<?> queue, HttpCrawlerConfig crawlerConfig, boolean resume, String[] seeds, ICrawler[] crawlers) {
		
		//, int crawlers, boolean strict, boolean respectRobots,int depth , 
		
		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		final UrlValidator validator = new UrlValidator();
		String[] validSeeds = (String[]) Arrays.stream(seeds)
        	.filter(seed -> seed != null)
        	.filter(seed -> validator.isValid(seed))
        	.collect(Collectors.toList()).toArray();
		
		if(validSeeds.length <= 0) {
			return false;
		}
        
		
		// BasicCrawler.generalConfiguration(userAgent, new File("./work"), crawlers, 300l, depth, strict, respectRobots, seeds)
		crawlerConfig.setStartURLs(validSeeds);
//		config.setCrawlerConfigs(crawlerConfig);
		
//		this.
//		collector = new HttpCollector(config);
//		
//		collector.setCrawlers(crawlers);
		
		start(resume);
//		collector.stop();
		
		
		// Need to create a custom HttpImporterPipeline to explain the picking strategy and where to send content (i.e. M52)
		// checkout HttpDocument (to retrieve html content), HttpMetadata (to add scores) classes 
		// Need to create create a custom CircularFifoQueue (i.e. custom queue not based on fifo strategy) that orders queue based on score)
		// i.e. priority queue with comparitor based on url score - score is updated on each visit (i.e. has it changed and in what way)
		
		
		// need to implement a  new version of GenericRecrawlableResolver - not reliant on sitemap and matches site/domain/sub-domain of interest
		// custom DocumentFetcher to add document to M52 queue
		
		// Question where and when is crawl queue affected - need to able to dynamically add and remove elements from the queue and (DONE) add a priority weight to the ordering 
 
		// DONE
		// Allow ignore noindex and nofollow options
		
		// DONE - need to add more metadata fields as calculations increase
		// ability tostore statistics, meta-data about a page custom ImporterDocument/HttpDocument
		// weight based on fifo as well as likelihood of producing new links and content.
		
		//Partial
		// custom IDocumentFilter to ignore pages that don't match the url rules (i.e. out of domain, sub-domain, regex filter(already exists)
		
		// Need to produce a set of stages, potentially for a custom committer pipeline - see class HttpCommitterPipeline
		// Deals with checksum, postprocessing etc...
		
		
		// when a page is recrawled need to index postgres table with a retrievabke id to overwrite old content that has been updated.
		// HttpDocument.reference - need to be able to index into postgres table for indiviual posts etc...
		
		return true;
	}

}
