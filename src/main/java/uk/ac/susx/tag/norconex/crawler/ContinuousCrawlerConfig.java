package uk.ac.susx.tag.norconex.crawler;

// java imports
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Norconex imports
import com.norconex.collector.core.checksum.impl.MD5DocumentChecksummer;
import com.norconex.collector.core.filter.impl.RegexReferenceFilter;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.collector.http.crawler.URLCrawlScopeStrategy;
import com.norconex.collector.http.data.store.impl.jdbc.JDBCCrawlDataStoreFactory;
import com.norconex.collector.http.delay.impl.GenericDelayResolver;
import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.url.impl.GenericLinkExtractor;
import com.norconex.importer.ImporterConfig;

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
public class ContinuousCrawlerConfig extends HttpCrawlerConfig {
	
	protected static final Logger logger = LoggerFactory.getLogger(ContinuousCrawlerConfig.class);
	
	public ContinuousCrawlerConfig(String userAgent, int depth, int crawlers, File crawlStore, 
			boolean respectRobots, boolean ignoreSiteMap, String id, List<String> regxFiltPatterns,
			BlockingQueue<HttpDocument> outputQueue, String seed,
			double rate) {
		
		// Basic crawler config
		setUserAgent(userAgent);
		setMaxDepth(depth); // -1 for inf
		setIgnoreRobotsMeta(respectRobots);
		setIgnoreRobotsTxt(respectRobots);
		setIgnoreCanonicalLinks(false);
		setIgnoreSitemap(ignoreSiteMap);
		
		
		// Control the number of crawlers by the number of threads
		setNumThreads(crawlers);
		
		// Location of crawl output, db etc... 
		setWorkDir(crawlStore);
		
		// only store a crawl cache M52 deals with content
		setKeepDownloads(false);
		setId(id);
		
		// Page found but record of its parent lost - process the content and links anyway
		setOrphansStrategy(OrphansStrategy.PROCESS);
		
		// Keeps the crawler within the same domain
		URLCrawlScopeStrategy ucs = new URLCrawlScopeStrategy();
		ucs.setStayOnDomain(true);
		ucs.setStayOnPort(false);
		ucs.setStayOnProtocol(false);
		setUrlCrawlScopeStrategy(ucs);
				
		// set to false so crawl cache is only those of interest
		setKeepOutOfScopeLinks(false);
		
		// use this if you want to adhere to sitemap.
		if(!ignoreSiteMap) {
			setStartSitemapURLs(seed);
		}
		
		// set this to correctly manage file sizes etc... 
		ImporterConfig importCon = new ImporterConfig();
		importCon.setMaxFileCacheSize(100);
		importCon.setMaxFilePoolCacheSize(100);
		importCon.setTempDir(crawlStore);
		setImporterConfig(importCon);
							
		// Used to set the politeness delay for consecutive post calls to the site (helps prevent being blocked)
		GenericDelayResolver gdr = new GenericDelayResolver();
		gdr.setDefaultDelay(300);
		gdr.setIgnoreRobotsCrawlDelay(respectRobots);
		gdr.setScope(GenericDelayResolver.SCOPE_CRAWLER);
		setDelayResolver(gdr);
		
		// Using generic as this is only used for politeness
		// RecrawlablResolver deals with specific web page recrawl delay
//		ContinuousDelayResolver delayResolve = new ContinuousDelayResolver();
//		delayResolve.setDefaultDelay(ContinuousController.DEFAULT_DELAY); // 12hrs as starting default
//		setDelayResolver(delayResolve);
		
		GenericLinkExtractor gle = new GenericLinkExtractor();
		gle.setIgnoreNofollow(respectRobots);
		gle.setCharset(StandardCharsets.UTF_8.toString());
		setLinkExtractors(gle);
		
		// custom fetcher or postimporter to send to M52 queue
		setPostImportProcessors(new Method52PostProcessor(outputQueue));		
		
		// create the url filters - e.g. regex filters
		// url regex match 
		// parent link prevention
		RegexReferenceFilter[] referenceFilters = regxFiltPatterns.stream()
			.map(regex -> new RegexReferenceFilter(regex))
			.collect(Collectors.toList()).toArray(new RegexReferenceFilter[regxFiltPatterns.size()]);
		setReferenceFilters(referenceFilters);
		
		// set our recrawlable resolver MAYBE needed instead of delay - look into 
		ContinuousRecrawlableResolver recrawlableResolver = new ContinuousRecrawlableResolver(rate,ignoreSiteMap);
		setRecrawlableResolver(recrawlableResolver);
		
		// forum to be done outside of m52
		// TODO: Potential issues on sites with heavy amounts of dynamic boilerplate?
		setDocumentChecksummer(new MD5DocumentChecksummer());
		HttpCrawler
		
		// Implement a shutdown listener that runs the crawler continually until shutdown sent by M52
		// Implement crawler listener - restart crawler every time it ends
		// Implement burn-in period
		// Add support for header metadata
		
		
		// Index on forum-post-id so that there are no duplicates
		// i.e. when creating a forum, webpage splitter create an id based on url and position on page
		
		
		// Need a custom committer for continuous crawler! 
//		cConfig.setCommitter((ICommitter) new HttpCommitterPipeline());
	
		
		// custom filter actually needed?
		// save url of files and crawl outside of m52
//		setDocumentFilters(documentfilters);


		
		// set our custom crawl data store
		// using default is fine.
		//priority queue possible
//		cConfig.setCrawlDataStoreFactory();
//		 create custom MVStoreCrawlDataStore override method: nextQueued
		
		// implement a burn-in period
		// i.e. until re-crawls > 10, re-crawl every 5 hours (e.g. burn-in over 1 week)
		
	}
	
	// TODO: Notes - after thoughts
	
	// override mv store to enable queuer order.
	// store queue order as initial metadata
	//	MVStoreCrawlDataStore store = new MVStoreCrawlDataStore("",false);
	//	store.
	//	cConfig.setCrawlDataStoreFactory(store);
	
	// generate a sitemap.xml?
	// read urls from list each time and check last crawled, priority, delay etc... 	

}
