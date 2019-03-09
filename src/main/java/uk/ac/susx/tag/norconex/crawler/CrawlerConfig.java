package uk.ac.susx.tag.norconex.crawler;

import java.io.File;

import com.norconex.collector.http.HttpCollectorConfig;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.collector.http.crawler.URLCrawlScopeStrategy;
import com.norconex.collector.http.delay.impl.GenericDelayResolver;

public class CrawlerConfig {
	
	public static HttpCrawlerConfig buildConfigurationFile(String userAgent, File crawlStore, int crawlers, long delay, int depth, boolean strict, boolean respectRobots, String... seeds) {
		
		HttpCrawlerConfig cConfig = new HttpCrawlerConfig();
		cConfig.setMaxDepth(depth);
		cConfig.setIgnoreRobotsMeta(respectRobots);
		cConfig.setIgnoreRobotsTxt(respectRobots);
		cConfig.setIgnoreCanonicalLinks(false);
//		cConfig.setCrawlDataStoreFactory(new JDBCCrawlDataStoreFactory());
		cConfig.setIgnoreSitemap(true);
		cConfig.setNumThreads(crawlers);
		cConfig.setWorkDir(crawlStore);
//		cConfig.setCommitter((ICommitter) new HttpCommitterPipeline());
		cConfig.setKeepDownloads(false);
		cConfig.setKeepOutOfScopeLinks(false);
		cConfig.setStartURLs(seeds);
		cConfig.setId(userAgent);
//		cConfig.setUrlCrawlScopeStrategy(urlCrawlScopeStrategy);
		
		GenericDelayResolver delayResolve = new GenericDelayResolver();
		delayResolve.setDefaultDelay(delay);
		cConfig.setDelayResolver(delayResolve);	
		
		URLCrawlScopeStrategy ucs = new URLCrawlScopeStrategy();
		ucs.setStayOnDomain(strict);
		ucs.setStayOnPort(false);
		ucs.setStayOnProtocol(false);
		
		cConfig.setUrlCrawlScopeStrategy(ucs);
		
		return cConfig;
				
	}
	
	public static HttpCollectorConfig buildCollectorConfig(String userAgent) {
	
		HttpCollectorConfig config = new HttpCollectorConfig();
//		cConfig.setId(userAgent);
		config.setId(userAgent);
//		config.setCrawlerConfigs(cConfig);
		return config;
	}
	
//	public static void main(String[] args) {
////		String url = "https://www.gamespot.com/forums/offtopic-discussion-314159273/i-need-some-car-purchase-advice-33451831";
//		String url = "https://www.nadis.org.uk/disease-a-z/poultry/diseases-of-farmyard-poultry/part-4-external-and-internal-parasites-of-chickens/";
//		HttpCollectorConfig hcc = CrawlerConfig.buildConfigurationFile("test-necr", new File("./work"), 2, 300l,1, true, url);
//		HttpCollector hc = new HttpCollector(hcc);
//		hc.start(false);
//	}

}
