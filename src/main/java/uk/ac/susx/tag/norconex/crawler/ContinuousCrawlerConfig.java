package uk.ac.susx.tag.norconex.crawler;

// java imports
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

// logging imports
import com.norconex.collector.core.checksum.impl.GenericMetadataChecksummer;
import com.norconex.collector.core.doc.CollectorMetadata;
import com.norconex.collector.http.fetch.impl.GenericMetadataFetcher;
import com.norconex.importer.parser.GenericDocumentParserFactory;
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
import uk.ac.susx.tag.norconex.document.ArticleExtractorChecksum;

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
								   boolean ignoreRobots, boolean ignoreSiteMap, String id,
								   List<String> regxFiltPatterns, long politeness,
								   String... seeds) {
		
		// Basic crawler config
		setUserAgent(userAgent);
		setMaxDepth(depth); // -1 for inf
		setIgnoreRobotsMeta(ignoreRobots);
		setIgnoreRobotsTxt(ignoreRobots);
		setIgnoreCanonicalLinks(true);
		setDocumentChecksummer(new ArticleExtractorChecksum());
		setIgnoreSitemap(ignoreSiteMap);

		// Control the threadpool size for each crawler
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

		setStartURLs(seeds);

		// use this if you want to adhere to sitemap.
		if(!ignoreSiteMap) {
			setStartSitemapURLs(seeds);
		}

		// set this to correctly manage file sizes etc... 
		ImporterConfig importCon = new ImporterConfig();
		importCon.setMaxFileCacheSize(10);
		importCon.setMaxFilePoolCacheSize(10);
		GenericDocumentParserFactory gdpf = new GenericDocumentParserFactory();
		gdpf.setIgnoredContentTypesRegex(".*");
		importCon.setParserFactory(gdpf);
		importCon.setTempDir(crawlStore);
		setImporterConfig(importCon);
							
		// Used to set the politeness delay for consecutive post calls to the site (helps prevent being blocked)
		GenericDelayResolver gdr = new GenericDelayResolver();
		gdr.setDefaultDelay((politeness <= 50) ? 50 : politeness); // safety check to avoid to to small a delay
		gdr.setIgnoreRobotsCrawlDelay(ignoreRobots);
		gdr.setScope(GenericDelayResolver.SCOPE_SITE);
		setDelayResolver(gdr);
		
		GenericLinkExtractor gle = new GenericLinkExtractor();
		gle.setIgnoreNofollow(ignoreRobots);
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
