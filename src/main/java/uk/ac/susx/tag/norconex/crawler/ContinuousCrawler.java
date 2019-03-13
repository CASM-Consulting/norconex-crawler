package uk.ac.susx.tag.norconex.crawler;

import java.io.File;

import com.norconex.collector.http.crawler.HttpCrawlerConfig;

public class ContinuousCrawler {
	
	public static HttpCrawlerConfig crawlerConfiguration(String userAgent, File crawlStore, long delay, int depth, boolean strict, boolean respectRobots, boolean siteMap) {
		
		HttpCrawlerConfig config = BasicCrawler.basicConfiguration(userAgent, crawlStore, delay, depth, strict, respectRobots, siteMap);
		
		return config;
		
	}

}
