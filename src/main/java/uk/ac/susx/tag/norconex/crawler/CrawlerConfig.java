package uk.ac.susx.tag.norconex.crawler;

import com.norconex.collector.http.HttpCollectorConfig;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;

public class CrawlerConfig {
	
	
	
	public HttpCollectorConfig buildConfiguration(String id) {
		HttpCollectorConfig config = new HttpCollectorConfig();
		config.setId(id);
		
		HttpCrawlerConfig cConfig = new HttpCrawlerConfig();
		cConfig.setMaxDepth(1);
		cConfig.setIgnoreRobotsMeta(true);
		cConfig.setIgnoreRobotsTxt(true);
		cConfig.setIgnoreCanonicalLinks(false);
		cConfig.set
		
		return config;
				
	}
	
	

}
