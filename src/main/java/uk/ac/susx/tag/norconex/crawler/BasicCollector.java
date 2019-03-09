package uk.ac.susx.tag.norconex.crawler;

import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import org.apache.commons.validator.UrlValidator;
import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.http.HttpCollector;
import com.norconex.collector.http.HttpCollectorConfig;
import com.norconex.collector.http.delay.impl.CrawlerDelay;
import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.fetch.HttpFetchResponse;

public class BasicCollector {
	
	protected static final Logger logger = LoggerFactory.getLogger(BasicCollector.class);
	
	private HttpCollector controller;

	private String userAgent;
	
	public BasicCollector(String userAgent) {
		this.userAgent = userAgent;
	}
	
	public boolean crawl(BlockingQueue<?> queue, List<String> seeds, CrawlerConfig config, int crawlers, boolean strict, boolean respectRobots,int depth) {
		
		
		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		final UrlValidator validator = new UrlValidator();
		seeds.stream()
        	.filter(seed -> seed != null)
        	.filter(seed -> validator.isValid(seed))
        	.collect(Collectors.toList());
         	
		HttpCollectorConfig collectorConfig = CrawlerConfig.buildCollectorConfig(userAgent);
		collectorConfig.setCrawlerConfigs(CrawlerConfig.buildConfigurationFile(userAgent, new File("./work"), crawlers, 300l, depth, strict, respectRobots, seeds.toArray(new String[seeds.size()])));
		controller = new HttpCollector(collectorConfig);
		
		
		// Need to create a custom HttpImporterPipeline to explain the picking strategy and where to send content (i.e. M52)
		// checkout HttpDocument (to retrieve html content), HttpMetadata (to add scores) classes 
		// Need to create create a custom CircularFifoQueue (i.e. custom queue not based on fifo strategy) that orders queue based on score)
		// i.e. priority queue with comparitor based on url score - score is updated on each visit (i.e. has it changed and in what way)
		// need to implement a  new version of GenericRecrawlableResolver - not reliant on sitemap and matches site/domain/sub-domain of interest
		
		// when a page is recrawled need to index postgres table with a retrievabke id to overwrite old content that has been updated.
		controller.start(true);
		
		return true;
	}
	

}
