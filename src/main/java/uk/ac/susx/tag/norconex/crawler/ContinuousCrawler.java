package uk.ac.susx.tag.norconex.crawler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import com.norconex.collector.core.crawler.ICrawlerConfig.OrphansStrategy;
import com.norconex.collector.core.data.store.impl.mvstore.MVStoreCrawlDataStore;
import com.norconex.collector.core.filter.impl.RegexReferenceFilter;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.collector.http.crawler.URLCrawlScopeStrategy;
import com.norconex.collector.http.data.store.impl.jdbc.JDBCCrawlDataStoreFactory;
import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.recrawl.impl.GenericRecrawlableResolver;
import com.norconex.collector.http.url.impl.GenericLinkExtractor;

import uk.ac.susx.tag.norconex.document.Method52PostProcessor;


/**
 * Implemented to simulate a scoped, continuous crawl
 * i.e. scoped within a specific domain, sub-directory or sub-domain of a site.
 * continuous as it conceptually never stops crawling but only downloads or checks for download 
 * according to likelihood of page change
 * 
 * Use rate to control the delay calculated by the burn-in period
 * This model is designed to maximise freshness 
 * i.e. 
 * @author jp242
 *
 */
public class ContinuousCrawler {
	
	public static HttpCrawlerConfig crawlerConfiguration(String userAgent, int depth, int crawlers, File crawlStore, 
			boolean respectRobots, boolean ignoreSiteMap, String id, List<String> regxFiltPatterns,
			boolean forum, BlockingQueue<HttpDocument> outputQueue, String seed,
			double rate, double recrawlFrequency) {
		
		HttpCrawlerConfig cConfig = BasicCrawler.basicConfiguration(userAgent, crawlStore, 0, depth, crawlers,true, respectRobots, ignoreSiteMap);
		
		cConfig.setUserAgent(userAgent);
		cConfig.setMaxDepth(depth); // -1 for inf
		cConfig.setIgnoreRobotsMeta(respectRobots);
		cConfig.setIgnoreRobotsTxt(respectRobots);
		cConfig.setIgnoreCanonicalLinks(false);
		cConfig.setCrawlDataStoreFactory(new JDBCCrawlDataStoreFactory());
		cConfig.setIgnoreSitemap(ignoreSiteMap);
		// Control the number of crawlers by the number of threads
		cConfig.setNumThreads(crawlers);			
		cConfig.setWorkDir(crawlStore);
		
		// only store a crawl cache M52 deals with content
		cConfig.setKeepDownloads(false);
		cConfig.setId(id);
		cConfig.setIgnoreSitemap(ignoreSiteMap);
		
		cConfig.setOrphansStrategy(OrphansStrategy.PROCESS);
		
		URLCrawlScopeStrategy ucs = new URLCrawlScopeStrategy();
		ucs.setStayOnDomain(true);
		ucs.setStayOnPort(false);
		ucs.setStayOnProtocol(false);
		cConfig.setUrlCrawlScopeStrategy(ucs);
				
		// set to false so crawl cache is only those of interest
		cConfig.setKeepOutOfScopeLinks(false);
		
		// use this if you want to adhere to sitemap.
		if(!ignoreSiteMap) {
			cConfig.setStartSitemapURLs(seed);
		}
		
		
		// override mv store to enable queuer order.
		// store queue order as initial metadata
//		MVStoreCrawlDataStore store = new MVStoreCrawlDataStore("",false);
//		store.
//		cConfig.setCrawlDataStoreFactory(store);
		
		// set this to correctly manage different document types. 
//		cConfig.setImporterConfig(importerConfig);
		
		// question how does it store urls - checksum etc....
		// daily run, iterates over list ordered by delay/priority (sitemap)
		// checks for new links AND new content
		// pre-flight (does site have sitemap?)
			
		// custom delay resolver (could this be used to implement order?)
//		ReferenceDelayResolver rdr = new ReferenceDelayResolver();
		// Add default delay for files types too - e.g. pdfs
		ContinuousDelayResolver delayResolve = new ContinuousDelayResolver();
		delayResolve.setDefaultDelay(43200000); // 12hrs as starting default
		cConfig.setDelayResolver(delayResolve);
		
		GenericLinkExtractor gle = new GenericLinkExtractor();
		gle.setIgnoreNofollow(respectRobots);
		gle.setCharset(StandardCharsets.UTF_8.toString());
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
		
		// set our recrawlable resolver MAYBE needed instead of delay - look into 
		ContinuousRecrawlableResolver recrawlableResolver = new ContinuousRecrawlableResolver(regxFiltPatterns,0.1);
		cConfig.setRecrawlableResolver(recrawlableResolver);
		
		// Implement a shutdown listener that runs the crawler continually until shutdown sent by M52
		// Implement crawler listener - restart crawler every time it ends
		// Implement burn-in period
		// Add support for header metadata 
		
		
		// Index on forum-post-id so that there are no duplicates
		// i.e. when creating a forum, webpage splitter create an id based on url and position on page
		
		
		// Need a custom committer for continuous crawler! 
//		cConfig.setCommitter((ICommitter) new HttpCommitterPipeline());
		
		// generate a sitemap.xml?
		// read urls from list each time and check last crawled, priority, delay etc... 		
		
		// TODO
		//prevent document download -> just store url 
				
		// Look into creating a custom sitemap impl for each continuous crawler
		// set proirity there
		
		// custom filter actually needed?
		// save url of files and crawl outside of m52
//		cConfig.setDocumentFilters(documentfilters);

		// create custom checksummer for forum?
		// forum to be done outside of m52
//		cConfig.setDocumentChecksummer(documentChecksummer);
		
		// set our custom crawl data store
		// using default is fine.
		//priority queue possible
//		cConfig.setCrawlDataStoreFactory();
//		 create custom MVStoreCrawlDataStore override method: nextQueued
		
		// implement a burn-in period
		// i.e. until re-crawls > 10, re-crawl every 5 hours (e.g. burn-in over 1 week)
		
		return cConfig;
		
	}

}
