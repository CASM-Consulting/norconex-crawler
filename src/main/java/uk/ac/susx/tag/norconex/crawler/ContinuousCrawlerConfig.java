package uk.ac.susx.tag.norconex.crawler;

// java imports
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

// logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Norconex imports
import com.norconex.collector.core.checksum.impl.MD5DocumentChecksummer;
import com.norconex.collector.core.filter.impl.RegexReferenceFilter;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.collector.http.crawler.URLCrawlScopeStrategy;
import com.norconex.collector.http.delay.impl.GenericDelayResolver;
import com.norconex.collector.http.url.impl.GenericLinkExtractor;
import com.norconex.importer.ImporterConfig;

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

	public ContinuousCrawlerConfig(String userAgent, int depth, int crawlers, File crawlStore, 
			boolean respectRobots, boolean ignoreSiteMap, String id, List<String> regxFiltPatterns,
			String seed) {
		
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

		setStartURLs(seed);
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
		gdr.setDefaultDelay(400);
		gdr.setIgnoreRobotsCrawlDelay(respectRobots);
		gdr.setScope(GenericDelayResolver.SCOPE_CRAWLER);
		setDelayResolver(gdr);
		
		GenericLinkExtractor gle = new GenericLinkExtractor();
		gle.setIgnoreNofollow(respectRobots);
		gle.setCharset(StandardCharsets.UTF_8.toString());
		setLinkExtractors(gle);	
		
		// create the url filters - e.g. regex filters
		// url regex match 
		// parent link prevention
		RegexReferenceFilter[] referenceFilters = regxFiltPatterns.stream()
			.map(regex -> new RegexReferenceFilter(regex))
			.collect(Collectors.toList()).toArray(new RegexReferenceFilter[regxFiltPatterns.size()]);
		setReferenceFilters(referenceFilters);

	}	

}
