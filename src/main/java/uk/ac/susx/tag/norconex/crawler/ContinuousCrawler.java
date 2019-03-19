package uk.ac.susx.tag.norconex.crawler;

import java.io.File;

import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.collector.http.crawler.URLCrawlScopeStrategy;
import com.norconex.collector.http.data.store.impl.jdbc.JDBCCrawlDataStoreFactory;
import com.norconex.collector.http.delay.impl.GenericDelayResolver;
import com.norconex.collector.http.url.impl.GenericLinkExtractor;
import com.norconex.collector.http.url.impl.GenericURLNormalizer;

public class ContinuousCrawler {
	
	public static HttpCrawlerConfig crawlerConfiguration(String userAgent, int crawlers, File crawlStore, 
			boolean respectRobots, boolean ignoreSiteMap, String id) {
		
		HttpCrawlerConfig cConfig = BasicCrawler.basicConfiguration(userAgent, crawlStore, 0, -1, true, respectRobots, ignoreSiteMap);
		
		cConfig.setUserAgent(userAgent);
		cConfig.setMaxDepth(-1);
		cConfig.setIgnoreRobotsMeta(respectRobots);
		cConfig.setIgnoreRobotsTxt(respectRobots);
		cConfig.setIgnoreCanonicalLinks(false);
		cConfig.setCrawlDataStoreFactory(new JDBCCrawlDataStoreFactory());
		cConfig.setIgnoreSitemap(ignoreSiteMap);
		// Control the number of crawlers by the number of threads
		cConfig.setNumThreads(crawlers);			
		cConfig.setWorkDir(crawlStore);
		cConfig.setKeepDownloads(false);
		cConfig.setId(id);
		cConfig.setIgnoreSitemap(ignoreSiteMap);
		URLCrawlScopeStrategy ucs = new URLCrawlScopeStrategy();
		ucs.setStayOnDomain(true);
		ucs.setStayOnPort(false);
		ucs.setStayOnProtocol(false);
		cConfig.setUrlCrawlScopeStrategy(ucs);
		// set to false so crawl cache is only those of interest
		cConfig.setKeepOutOfScopeLinks(false);
			
		// custom delay resolver (could this be used to implement order?)
		GenericDelayResolver delayResolve = new GenericDelayResolver();
		delayResolve.setDefaultDelay(86000000); // 1 day as starting default
		cConfig.setDelayResolver(delayResolve);
		
		GenericLinkExtractor gle = new GenericLinkExtractor();
		gle.setIgnoreNofollow(respectRobots);
		cConfig.setLinkExtractors(gle);
				
		// need this or urlfilter to check the section of the web site.
//		GenericURLNormalizer gun = new GenericURLNormalizer();
//		gun.setDisabled(false);
//		gun.setNormalizations(normalizations);
		
		// custom for forum scrapers
//		cConfig.setMetadataChecksummer(metadataChecksummer);
		
		// custom filter actually needed?
//		cConfig.setDocumentFilters(documentfilters);
		
		// custom fetcher or postimporter to send to M52 queue
//		cConfig.setDocumentFetcher(documentFetcher);		
//		cConfig.setPostImportProcessors(httpPostProcessors);

		
		// create custom checksummer
//		cConfig.setDocumentChecksummer(documentChecksummer);

		// create the url filters - e.g. regex filters
		// url regex match 
		// parent link prevention
//		cConfig.setReferenceFilters(referenceFilters);
		
		// set our recrawlable resolver 
//		cConfig.setRecrawlableResolver(recrawlableResolver);
		
		// set our custom crawl data store
//		cConfig.setCrawlDataStoreFactory();
//		 create custom MVStoreCrawlDataStore override method: nextQueued
		
		// Need a custom committer for continuous crawler! 
//		cConfig.setCommitter((ICommitter) new HttpCommitterPipeline());
		
		return cConfig;
		
	}

}
