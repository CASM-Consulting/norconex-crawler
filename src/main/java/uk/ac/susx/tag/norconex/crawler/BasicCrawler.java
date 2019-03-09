package uk.ac.susx.tag.norconex.crawler;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.http.crawler.HttpCrawler;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;

public class BasicCrawler extends HttpCrawler {
	
	protected static final Logger logger = LoggerFactory.getLogger(BasicCrawler.class);
	
	private List<String> domains;		// domain restrictions (null or empty means there are none).
    private UrlValidator validator;
	
	public BasicCrawler(BlockingQueue<?> queue, HttpCrawlerConfig crawlerConfig, boolean strict) {
		super(crawlerConfig);
		validator = new UrlValidator();
		 
	}

}
