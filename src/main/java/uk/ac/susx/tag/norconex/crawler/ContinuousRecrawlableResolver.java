package uk.ac.susx.tag.norconex.crawler;

// logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// norconcex imports
import com.norconex.collector.http.recrawl.PreviousCrawlData;
import com.norconex.collector.http.recrawl.impl.GenericRecrawlableResolver;

import uk.ac.susx.tag.norconex.controller.ContinuousController;
import uk.ac.susx.tag.norconex.crawler.ContinuousEstimatorStore.ContinuousMetadata;

/**
 * Used to control whether this site should be recrawled based on url match
 * Acts as a safety net in case new urls not in domain etc... slip the net.
 * Not to be confused with crawl priority
 * @author jp242
 */
public class ContinuousRecrawlableResolver extends GenericRecrawlableResolver {
	
	protected static final Logger logger = LoggerFactory.getLogger(ContinuousRecrawlableResolver.class);

//	private double rate;          // parameter to control the interval calculation
	boolean siteMap;
	private ContinuousEstimatorStore store;
		
	public ContinuousRecrawlableResolver(boolean siteMap, ContinuousEstimatorStore conStats) {
		
		store = conStats;
		
		if(siteMap) {
			this.setSitemapSupport(SitemapSupport.LAST);
		}
		else {
			this.setSitemapSupport(SitemapSupport.NEVER);
		}
		this.siteMap = siteMap;

	}
	
	/**
	 * Checks to see if the specified date has passed for the url to be recrawled
	 */
	@Override
    public boolean isRecrawlable(PreviousCrawlData prevData) {
		
		// get the crawl stats for the url
		ContinuousMetadata crawlStats = store.getURLMetadata(prevData.getReference());
		
		// If it's not been crawled before then crawl and add a new set of stats to the store
		if(prevData.getCrawlDate() == null || crawlStats == null) {
			crawlStats = store.addMetadata(prevData.getReference());
			return true;
		}

		// if sitemap enabled then backoff to generic implementation
		if(siteMap) {
			return super.isRecrawlable(prevData);
		}
		
		// If enough stats have not yet been collected to estimate the delay then recrawl
		if(crawlStats.getCheckedCount() < ContinuousController.BURNIN_CRAWLS) {
			return true;
		}

		if(System.currentTimeMillis() >= crawlStats.getNextCrawl()) {
			crawlStats.incrementCheckedCount();
			crawlStats.setNextCrawl(store.estimateCrawlInterval(store.estimateChangeFrequency(crawlStats.getCheckedCount(),crawlStats.getChangeCount())));
			return true;
		}

		return false;
	}
	

//	// implementation of continuous delay
//	public long calculateFreshnessDelay(PreviousCrawlData prevCrawl, long delay) {
//		
//		long numChanges = Long.valueOf(prevCrawl.getSitemapChangeFreq());
//		
//		long priority = 0l;
//		
//		long newDelay = 0l;
//		
//		return newDelay;
//		
//	}
	
//	public long estimateFreshness(PreviousCrawlData prevCrawl) {
//		return 0l;
//	}
	


	
	
	// set minimum to 24hrs to begin
	// crawl the site completely once to begin the process 
	// 				have a check that deals with the first crawl special case
	// only adapt delay after 3rd crawl of site
	// implement no parents, no loops, no out of domain/url regex
	// checksum that checks if page has changed. 
	// 			if forum scrape then check the posts
	//			if page check the overall content of the page
	//					- what about boilerplate?
	// 
	

}
