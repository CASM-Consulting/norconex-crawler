package uk.ac.susx.tag.norconex.crawler;

import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.http.crawler.HttpCrawler;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.collector.http.crawler.URLCrawlScopeStrategy;
import com.norconex.collector.http.data.store.impl.jdbc.JDBCCrawlDataStoreFactory;
import com.norconex.collector.http.delay.impl.GenericDelayResolver;
import com.norconex.collector.http.pipeline.committer.HttpCommitterPipeline;
import com.norconex.committer.core.ICommitter;

public class BasicCrawler extends HttpCrawler {
	
	protected static final Logger logger = LoggerFactory.getLogger(BasicCrawler.class);
	
//	private List<String> domains;		// domain restrictions (null or empty means there are none).
	
	public BasicCrawler(BlockingQueue<?> queue, HttpCrawlerConfig crawlerConfig) {
		super(crawlerConfig);
	}
	
	public static HttpCrawlerConfig basicConfiguration(String userAgent, File crawlStore, long delay, int depth, boolean strict, boolean respectRobots, boolean siteMap) {
		// int crawlers, 
		
		HttpCrawlerConfig cConfig = new HttpCrawlerConfig();
		cConfig.setUserAgent(userAgent);
		cConfig.setMaxDepth(depth);
		cConfig.setIgnoreRobotsMeta(respectRobots);
		cConfig.setIgnoreRobotsTxt(respectRobots);
		cConfig.setIgnoreCanonicalLinks(false);
		cConfig.setCrawlDataStoreFactory(new JDBCCrawlDataStoreFactory());
		cConfig.setIgnoreSitemap(siteMap);			
		cConfig.setNumThreads(1);					// Control the number of threads by the number of crawlers
		cConfig.setWorkDir(crawlStore);
		cConfig.setKeepDownloads(false);
		cConfig.setKeepOutOfScopeLinks(false);
		
		GenericDelayResolver delayResolve = new GenericDelayResolver();
		delayResolve.setDefaultDelay(delay);
		cConfig.setDelayResolver(delayResolve);	
		
		URLCrawlScopeStrategy ucs = new URLCrawlScopeStrategy();
		ucs.setStayOnDomain(strict);
		ucs.setStayOnPort(false);
		ucs.setStayOnProtocol(false);
		cConfig.setUrlCrawlScopeStrategy(ucs);
		
		// Need a custom committer for continuous crawler! 
		cConfig.setCommitter((ICommitter) new HttpCommitterPipeline());
		
		return cConfig;
				
	}

}
