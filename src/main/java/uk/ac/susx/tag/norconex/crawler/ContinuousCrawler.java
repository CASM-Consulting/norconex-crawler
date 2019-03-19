package uk.ac.susx.tag.norconex.crawler;

import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import com.norconex.collector.core.filter.impl.RegexReferenceFilter;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.collector.http.crawler.URLCrawlScopeStrategy;
import com.norconex.collector.http.data.store.impl.jdbc.JDBCCrawlDataStoreFactory;
import com.norconex.collector.http.delay.impl.ReferenceDelayResolver;
import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.recrawl.impl.GenericRecrawlableResolver;
import com.norconex.collector.http.url.impl.GenericLinkExtractor;

import uk.ac.susx.tag.norconex.document.Method52PostProcessor;

public class ContinuousCrawler {
	
	public static HttpCrawlerConfig crawlerConfiguration(String userAgent, int crawlers, File crawlStore, 
			boolean respectRobots, boolean ignoreSiteMap, String id, List<String> regxFiltPatterns,
			boolean forum, BlockingQueue<HttpDocument> outputQueue) {
		
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
//		ReferenceDelayResolver rdr = new ReferenceDelayResolver();
		ContinuousDelayResolver delayResolve = new ContinuousDelayResolver();
		delayResolve.setDefaultDelay(86000000); // 1 day as starting default
		cConfig.setDelayResolver(delayResolve);
		
		GenericLinkExtractor gle = new GenericLinkExtractor();
		gle.setIgnoreNofollow(respectRobots);
		cConfig.setLinkExtractors(gle);
		
		// custom fetcher or postimporter to send to M52 queue
		cConfig.setPostImportProcessors(new Method52PostProcessor(forum,outputQueue));
		
		// create the url filters - e.g. regex filters
		// url regex match 
		// parent link prevention
		RegexReferenceFilter[] referenceFilters = regxFiltPatterns.stream()
			.map(regex -> new RegexReferenceFilter(regex))
			.collect(Collectors.toList()).toArray(new RegexReferenceFilter[regxFiltPatterns.size()]);
		cConfig.setReferenceFilters(referenceFilters);
				
		// need this or urlfilter to check the section of the web site.
//		GenericURLNormalizer gun = new GenericURLNormalizer();
//		gun.setDisabled(false);
//		gun.setNormalizations(normalizations);
		
		// custom for forum scrapers
//		cConfig.setMetadataChecksummer(metadataChecksummer);
		
		// custom filter actually needed?
//		cConfig.setDocumentFilters(documentfilters);

		
		// create custom checksummer for forum?
//		cConfig.setDocumentChecksummer(documentChecksummer);

		// set our recrawlable resolver 
//		GenericRecrawlableResolver recrawlableResolver = new GenericRecrawlableResolver();
//		cConfig.setRecrawlableResolver(recrawlableResolver);
		
		// set our custom crawl data store
//		cConfig.setCrawlDataStoreFactory();
//		 create custom MVStoreCrawlDataStore override method: nextQueued
		
		// Need a custom committer for continuous crawler! 
//		cConfig.setCommitter((ICommitter) new HttpCommitterPipeline());
		
		return cConfig;
		
	}

}
