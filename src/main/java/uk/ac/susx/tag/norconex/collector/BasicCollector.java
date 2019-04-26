package uk.ac.susx.tag.norconex.collector;

// java imports
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

// logging imports
import com.norconex.importer.parser.GenericDocumentParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// norconcex imports
import com.norconex.collector.core.crawler.ICrawlerConfig.OrphansStrategy;
import com.norconex.collector.core.filter.impl.RegexReferenceFilter;
import com.norconex.collector.http.HttpCollector;
import com.norconex.collector.http.HttpCollectorConfig;
import com.norconex.collector.http.crawler.HttpCrawlerConfig;
import com.norconex.collector.http.crawler.URLCrawlScopeStrategy;
import com.norconex.collector.http.delay.impl.GenericDelayResolver;
import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.collector.http.url.impl.GenericLinkExtractor;
import com.norconex.importer.ImporterConfig;

import uk.ac.susx.tag.norconex.document.Method52PostProcessor;

/**
 * A simple crawler that spiders from a seed set of URLs and collects the content of discovered webpages.
 * This crawler runs until the specified depth or stopping condition and ends. 
 * @author jp242
 *
 */
public class BasicCollector extends HttpCollector {

	protected static final Logger logger = LoggerFactory.getLogger(BasicCollector.class);

	public BasicCollector(HttpCollectorConfig config) {
		super(config);
	}

	public static HttpCrawlerConfig crawlerConfig(String userAgent, int depth, int crawlers, File crawlStore,
												  boolean ignoreRobots, boolean ignoreSiteMap, String id, List<String> regxFiltPatterns,
												  BlockingQueue<HttpDocument> queue, boolean strictDomain) {

		HttpCrawlerConfig config = new HttpCrawlerConfig();

		// Basic crawler config
		config.setUserAgent(userAgent);
		config.setMaxDepth(depth); // -1 for inf
		config.setIgnoreRobotsMeta(ignoreRobots);
		config.setIgnoreRobotsTxt(ignoreRobots);
		config.setIgnoreCanonicalLinks(true);
		config.setIgnoreSitemap(ignoreSiteMap);

		// Control the number of crawlers by the number of threads
		config.setNumThreads(crawlers);

		// Location of crawl output, db etc...
		config.setWorkDir(crawlStore);

		// only store a crawl cache M52 deals with content
		config.setKeepDownloads(false);
		config.setId(id);

		// Page found but record of its parent lost - process the content and links anyway
		config.setOrphansStrategy(OrphansStrategy.PROCESS);

		// Keeps the crawler within the same domain
		URLCrawlScopeStrategy ucs = new URLCrawlScopeStrategy();
		ucs.setStayOnDomain(strictDomain);
		ucs.setStayOnPort(false);
		ucs.setStayOnProtocol(false);
		config.setUrlCrawlScopeStrategy(ucs);

		// set to false so crawl cache is only those of interest
		config.setKeepOutOfScopeLinks(false);

		// set this to correctly manage file sizes etc...
		ImporterConfig importCon = new ImporterConfig();
		importCon.setMaxFileCacheSize(100);
		importCon.setMaxFilePoolCacheSize(100);
		importCon.setTempDir(crawlStore);
		// For now - do not do any parsing on the discovered docs (i.e. all done by M52)
		GenericDocumentParserFactory gdpf = new GenericDocumentParserFactory();
		gdpf.setIgnoredContentTypesRegex(".*");
		importCon.setParserFactory(gdpf);
		config.setImporterConfig(importCon);


		// Used to set the politeness delay for consecutive post calls to the site (helps prevent being blocked)
		GenericDelayResolver gdr = new GenericDelayResolver();
		gdr.setDefaultDelay(300);
		gdr.setIgnoreRobotsCrawlDelay(ignoreRobots);
		gdr.setScope(GenericDelayResolver.SCOPE_CRAWLER);
		config.setDelayResolver(gdr);

		GenericLinkExtractor gle = new GenericLinkExtractor();
		gle.setIgnoreNofollow(ignoreRobots);
		gle.setCharset(StandardCharsets.UTF_8.toString());
		config.setLinkExtractors(gle);

		// create the url filters - e.g. regex filters
		// url regex match
		// parent link prevention
		RegexReferenceFilter[] referenceFilters = regxFiltPatterns.stream()
				.map(regex -> new RegexReferenceFilter(regex))
				.collect(Collectors.toList()).toArray(new RegexReferenceFilter[regxFiltPatterns.size()]);
		config.setReferenceFilters(referenceFilters);

		config.setPostImportProcessors(new Method52PostProcessor(queue));

		return config;

	}
}


//
//}
	
	// Need to create a custom HttpImporterPipeline to explain the picking strategy and where to send content (i.e. M52)
	// checkout HttpDocument (to retrieve html content), HttpMetadata (to add scores) classes 
	// Need to create create a custom CircularFifoQueue (i.e. custom queue not based on fifo strategy) that orders queue based on score)
	// i.e. priority queue with comparitor based on url score - score is updated on each visit (i.e. has it changed and in what way)
	
	
	// need to implement a  new version of GenericRecrawlableResolver - not reliant on sitemap and matches site/domain/sub-domain of interest
	// custom DocumentFetcher to add document to M52 queue
	
	// Question where and when is crawl queue affected - need to able to dynamically add and remove elements from the queue and (DONE) add a priority weight to the ordering 

	// DONE
	// Allow ignore noindex and nofollow options
	
	// DONE - need to add more metadata fields as calculations increase
	// ability tostore statistics, meta-data about a page custom ImporterDocument/HttpDocument
	// weight based on fifo as well as likelihood of producing new links and content.
	
	//Partial
	// custom IDocumentFilter to ignore pages that don't match the url rules (i.e. out of domain, sub-domain, regex filter(already exists)
	
	// Need to produce a set of stages, potentially for a custom committer pipeline - see class HttpCommitterPipeline
	// Deals with checksum, postprocessing etc...
	
	// when a page is recrawled need to index postgres table with a retrievabke id to overwrite old content that has been updated.
	// HttpDocument.reference - need to be able to index into postgres table for indiviual posts etc...
